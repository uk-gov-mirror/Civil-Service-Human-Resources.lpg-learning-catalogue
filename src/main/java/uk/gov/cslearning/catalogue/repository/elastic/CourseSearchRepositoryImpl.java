package uk.gov.cslearning.catalogue.repository.elastic;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.Utils;
import uk.gov.cslearning.catalogue.api.OwnerParameters;
import uk.gov.cslearning.catalogue.api.SearchResults;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Status;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
public class CourseSearchRepositoryImpl implements CourseSearchRepository {

    private ElasticsearchOperations operations;

    public CourseSearchRepositoryImpl(ElasticsearchOperations operations) {
        checkArgument(operations != null);
        this.operations = operations;
    }

    @Override
    public SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters, String sortField, Sort.Direction sortDirection) {
        Query searchQuery = getSearchQuery(pageable, courseSearchParameters, sortField, sortDirection).build();
        Page<Course> coursePage = Utils.searchPageToPage(operations.search(searchQuery, Course.class), pageable);
        return new SearchResults(coursePage, pageable);
    }

    @Override
    public SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters) {
        Query searchQuery = getSearchQuery(pageable, courseSearchParameters).build();
        Page<Course> coursePage = Utils.searchPageToPage(operations.search(searchQuery, Course.class), pageable);
        return new SearchResults(coursePage, pageable);
    }

    @Override
    public SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters, OwnerParameters ownerParameters) {
        BoolQueryBuilder filterQuery = getOwnerFilter(ownerParameters);
        Query searchQuery = getSearchQuery(pageable, courseSearchParameters)
                .withFilter(filterQuery).build();
        Page<Course> coursePage = Utils.searchPageToPage(operations.search(searchQuery, Course.class), pageable);
        return new SearchResults(coursePage, pageable);
    }

    private BoolQueryBuilder getSearchBuilderQuery(CourseSearchParameters parameters) {
        BoolQueryBuilder searchQuery = boolQuery();
        String query = parameters.getQuery();

        if (isNotBlank(query)) {
            searchQuery = searchQuery.must(multiMatchQuery(query)
                    .field("title", 8)
                    .field("shortDescription", 4)
                    .field("description", 2)
                    .field("learningOutcomes", 2)
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .fuzziness(Fuzziness.ONE)
                    .operator(Operator.AND));
        }

        if (parameters.getTitleStartsWith() != null) {
            searchQuery.must(new PrefixQueryBuilder("title.keyword", parameters.getTitleStartsWith().trim()).caseInsensitive(true));
        }

        if (parameters.hasModuleTypes()) {
            BoolQueryBuilder typesQuery = getModuleTypeBoolQuery(parameters.getTypes());
            searchQuery.must(typesQuery);
        }

        if (parameters.costIsFree()) {
            searchQuery.must(matchQuery("cost", 0).operator(Operator.AND));
        }

        if (parameters.hasAudienceFields()) {
            searchQuery.must(getAudienceNestedQuery(
                    parameters.getDepartments(),
                    parameters.getAreasOfWork(),
                    parameters.getInterests()));
        }

        List<String> statusList = parameters.getStatus().stream().map(Status::getValue).collect(Collectors.toList());
        searchQuery = addFilter(searchQuery, statusList, "status");

        if (!parameters.getCourseIds().isEmpty()) {
            searchQuery.filter(idsQuery().addIds(parameters.getCourseIds().toArray(new String[]{})));
        }

        return searchQuery;
    }

    private BoolQueryBuilder getModuleTypeBoolQuery(List<String> moduleTypes) {
        BoolQueryBuilder typesQuery = boolQuery();
        for (String type : moduleTypes) {
            typesQuery
                    .should(matchQuery("modules.type", type))
                    .should(matchQuery("type", type));
        }

        typesQuery.minimumShouldMatch(1);
        return typesQuery;
    }

    private NestedQueryBuilder getAudienceNestedQuery(List<String> departments, List<String> areasOfWork, List<String> interests) {
        BoolQueryBuilder audiencesBoolQuery = boolQuery();

        departments.forEach(department -> audiencesBoolQuery.must(matchQuery("audiences.departments", department).operator(Operator.AND)));
        areasOfWork.forEach(areaOfWork -> audiencesBoolQuery.must(matchQuery("audiences.areasOfWork", areaOfWork).operator(Operator.AND)));
        interests.forEach(interest -> audiencesBoolQuery.must(matchQuery("audiences.interests", interest).operator(Operator.AND)));

        return nestedQuery("audiences", audiencesBoolQuery, ScoreMode.Avg);
    }

    private BoolQueryBuilder getOwnerFilter(OwnerParameters ownerParameters) {

        BoolQueryBuilder filterQuery = boolQuery();

        if (ownerParameters.hasOrganisationalUnitCode()) {
            filterQuery.must(matchQuery("owner.organisationalUnit", ownerParameters.getOrganisationalUnitCode()));
        }

        if (ownerParameters.hasProfession()) {
            filterQuery.must(matchQuery("owner.profession", ownerParameters.getProfession()));
        }

        if (ownerParameters.hasSupplier()) {
            filterQuery.must(matchQuery("owner.supplier", ownerParameters.getSupplier()));
        }

        return filterQuery;
    }

    private NativeSearchQueryBuilder getSearchQuery(Pageable pageable, CourseSearchParameters courseSearchParameters, String fieldName,
                                                    Sort.Direction direction) {
        Script lowercase = new Script(String.format("doc['%s.keyword'].value.toLowerCase()", fieldName));
        SortBuilder<ScriptSortBuilder> sortBuilder = SortBuilders
                .scriptSort(lowercase, ScriptSortBuilder.ScriptSortType.STRING)
                .order(SortOrder.fromString(direction.name()));
        return getSearchQuery(courseSearchParameters)
                .withSort(sortBuilder)
                .withPageable(pageable);
    }

    private NativeSearchQueryBuilder getSearchQuery(CourseSearchParameters courseSearchParameters) {
        BoolQueryBuilder boolQuery = getSearchBuilderQuery(courseSearchParameters);
        BoolQueryBuilder filterQuery = boolQuery();

        if (courseSearchParameters.getVisibility().equals("PUBLIC")) {
            filterQuery.should(matchQuery("visibility", "PUBLIC"));
        }

        return new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withFilter(filterQuery);
    }

    private NativeSearchQueryBuilder getSearchQuery(Pageable pageable, CourseSearchParameters courseSearchParameters) {
        return getSearchQuery(courseSearchParameters)
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                .withPageable(pageable);
    }

    @Override
    public Page<Course> findAllByOrganisationCode(String organisationalUnitCode, Pageable pageable) {
        BoolQueryBuilder boolQuery = boolQuery();

        boolQuery.must(matchQuery("owner.organisationalUnit", organisationalUnitCode));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        return Utils.searchPageToPage(operations.search(searchQuery, Course.class), pageable);
    }

    @Override
    public Page<Course> findAllBySupplier(String supplier, Pageable pageable) {
        BoolQueryBuilder boolQuery = boolQuery();

        boolQuery.must(matchQuery("owner.supplier", supplier));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        return Utils.searchPageToPage(operations.search(searchQuery, Course.class), pageable);
    }


    private BoolQueryBuilder addFilter(BoolQueryBuilder boolQuery, List<String> values, String key) {
        if (values != null && !values.isEmpty()) {
            BoolQueryBuilder filterQuery = QueryBuilders.boolQuery();

            for (String department : values) {
                filterQuery = filterQuery
                        .should(QueryBuilders.matchPhraseQuery(key, department));
            }
            filterQuery.minimumShouldMatch(1);
            return boolQuery.must(filterQuery);
        }
        return boolQuery;
    }

}
