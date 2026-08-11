package me.ronygomes.ums.api.controller;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.UploadedFileDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.service.UploadService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("controller-test")
public class UploadControllerTest {

    @MockitoBean
    private UploadService uploadService;

    @MockitoBean
    private ExceptionHelper exceptionHelper;

    @MockitoBean
    private Validator validator;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testUploadSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transcript.pdf",
                "application/pdf",
                "hello world".getBytes());

        UUID id = UUID.randomUUID();
        UploadedFileDto transcript = new UploadedFileDto();
        transcript.setName("transcript.pdf");
        transcript.setUploadPath(id.toString());
        transcript.setFileSize(11L);
        transcript.setFiletype("application/pdf");

        ArgumentCaptor<MultipartFile> ac = ArgumentCaptor.forClass(MultipartFile.class);
        Mockito.when(uploadService.upload(ac.capture())).thenReturn(transcript);

        mockMvc.perform(multipart("/v1/upload").file(file))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.name").value("transcript.pdf"))
                .andExpect(jsonPath("$.uploadPath").value(id.toString()))
                .andExpect(jsonPath("$.fileSize").value(11))
                .andExpect(jsonPath("$.filetype").value("application/pdf"));

        MultipartFile received = ac.getValue();
        Assertions.assertEquals("transcript.pdf", received.getOriginalFilename());
        Assertions.assertEquals("application/pdf", received.getContentType());
        Assertions.assertEquals(11L, received.getSize());
    }

    @Test
    void testUploadValidationFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anything.bin",
                "application/octet-stream",
                "data".getBytes());

        Mockito.doThrow(new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED, "abc"))
                .when(exceptionHelper).throwErrorIfValidationError(Mockito.any(), Mockito.anyString());

        mockMvc.perform(multipart("/v1/upload").file(file))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/data-validation-failed"))
                .andExpect(jsonPath("$.title").value("Provided data is not valid"))
                .andExpect(jsonPath("$.detail").value("abc"))
                .andExpect(jsonPath("$.instance").value("/v1/upload"));

        Mockito.verify(uploadService, Mockito.never()).upload(Mockito.any());
    }
}
