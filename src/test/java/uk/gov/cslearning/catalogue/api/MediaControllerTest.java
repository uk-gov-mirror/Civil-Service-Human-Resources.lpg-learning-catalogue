package uk.gov.cslearning.catalogue.api;

import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cslearning.catalogue.config.FileUploadMap;
import uk.gov.cslearning.catalogue.domain.ErrorDtoFactory;
import uk.gov.cslearning.catalogue.domain.Media;
import uk.gov.cslearning.catalogue.dto.upload.FileUpload;
import uk.gov.cslearning.catalogue.service.upload.MediaManagementService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(MediaController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, MediaController.class,
                                ApiExceptionHandler.class, FileUploadMap.class})
public class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaManagementService mediaManagementService;

    @MockBean
    private ErrorDtoFactory errorDtoFactory;

    @Test
    public void shouldUploadFileOnPostRequest() throws Exception {
        String fileContainer = "container-id";
        String mediaId = "media-uid";
        String filename = "custom-filename";

        MockMultipartFile file = new MockMultipartFile("file", "file.doc", "application/octet-stream", "abc".getBytes());

        Media media = mock(Media.class);
        when(media.getId()).thenReturn(mediaId);
        when(mediaManagementService.create(any(FileUpload.class))).thenReturn(media);

        mockMvc.perform(
                multipart("/media")
                        .file(file)
                        .param("container", fileContainer)
                        .param("filename", filename)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "http://localhost/media/" + mediaId));
    }

    @Test
    public void shouldRejectInvalidFileExt() throws Exception {
        String fileContainer = "container-id";
        String mediaId = "media-uid";
        String filename = "custom-filename";

        MockMultipartFile file = new MockMultipartFile("file", "file.asp", "application/octet-stream", "abc".getBytes());

        mockMvc.perform(
                        multipart("/media")
                                .file(file)
                                .param("container", fileContainer)
                                .param("filename", filename)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn200OnSuccessfulGetRequest() throws Exception {
        String mediaId = "media-id";

        long filesize = 10;
        String container = "test-container";
        LocalDateTime date = LocalDateTime.now();
        String extension = "doc";
        String id = "media-id";
        String name = "filename";
        String path = "test-path";

        Media media = new Media();
        media.setFileSizeKB(filesize);
        media.setContainer(container);
        media.setDateAdded(date);
        media.setExtension(extension);
        media.setId(id);
        media.setName(name);
        media.setPath(path);

        when(mediaManagementService.findById(mediaId)).thenReturn(Optional.of(media));

        mockMvc.perform(
                get("/media/" + mediaId)
                        .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.container", is(container)))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.fileSizeKB", is((int) filesize)))
                .andExpect(jsonPath("$.extension", is(extension)))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.dateAdded", is(date.format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.path", is(path)));
    }

    @Test
    public void shouldReturn404IfMediaNotFound() throws Exception {
        String mediaUid = "media-uid";

        when(mediaManagementService.findById(mediaUid)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/media/" + mediaUid)
                        .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUploadImageFileOnPostRequest() throws Exception {
        String fileContainer = "container-id";
        String mediaId = "media-uid";
        String filename = "custom-filename";

        MockMultipartFile file = new MockMultipartFile("file", "file.jpg", "application/octet-stream", "abc".getBytes());
        Media media = mock(Media.class);
        when(media.getId()).thenReturn(mediaId);
        when(mediaManagementService.create(any(FileUpload.class))).thenReturn(media);

        mockMvc.perform(
                multipart("/media/skills/image")
                        .file(file)
                        .param("container", fileContainer)
                        .param("filename", filename)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "http://localhost/media/" + mediaId));
    }

    @Test
    public void shouldUploadImageFileIgnoreExtensionOnPostRequest() throws Exception {
        String fileContainer = "container-id";
        String mediaId = "media-uid";
        String filename = "custom-filename";

        MockMultipartFile file = new MockMultipartFile("file", "file.PNG", "application/octet-stream", "abc".getBytes());
        FileUpload fileUpload = FileUpload.createFromMetadata(file, fileContainer, filename);

        Media media = mock(Media.class);
        when(media.getId()).thenReturn(mediaId);
        when(mediaManagementService.create(any(FileUpload.class))).thenReturn(media);

        mockMvc.perform(
                multipart("/media/skills/image")
                        .file(file)
                        .param("container", fileContainer)
                        .param("filename", filename)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "http://localhost/media/" + mediaId));
    }

}
