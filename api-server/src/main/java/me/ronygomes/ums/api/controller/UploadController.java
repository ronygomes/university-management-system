package me.ronygomes.ums.api.controller;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.UploadedFileDto;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.service.UploadService;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/upload")
public class UploadController {

    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Upload. See 'error' field for details";

    private final UploadService uploadService;
    private final Validator validator;
    private final ExceptionHelper exceptionHelper;

    public UploadController(UploadService uploadService,
                            Validator validator,
                            ExceptionHelper exceptionHelper) {

        this.uploadService = uploadService;
        this.validator = validator;
        this.exceptionHelper = exceptionHelper;
    }

    @PostMapping
    public UploadedFileDto upload(@RequestParam("file") MultipartFile file) {
        UploadedFileDto dto = new UploadedFileDto();
        if (file != null) {
            dto.setName(file.getOriginalFilename());
            dto.setFileSize(file.getSize());
            dto.setFiletype(file.getContentType());
        }

        SpringValidatorAdapter beanValidator = new SpringValidatorAdapter(validator);
        BindingResult errors = new BeanPropertyBindingResult(dto, "uploadedFile");
        beanValidator.validate(dto, errors);

        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE);

        return uploadService.upload(file);
    }
}
