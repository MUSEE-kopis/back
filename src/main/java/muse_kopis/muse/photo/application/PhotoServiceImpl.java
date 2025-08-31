package muse_kopis.muse.photo.application;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.common.s3.S3Exception;
import muse_kopis.muse.ticketbook.domain.TicketBook;
import muse_kopis.muse.photo.domain.Photo;
import muse_kopis.muse.photo.domain.PhotoRepository;
import muse_kopis.muse.photo.infra.PhotoValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final static PhotoValidator validator = new PhotoValidator();
    private final PhotoRepository photoRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    @Override
    public String upload(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new S3Exception("빈 파일입니다.");
        }
        return this.uploadImage(image);
    }

    private String uploadImage(MultipartFile image) {
        validator.validateImageFileExtention(Objects.requireNonNull(image.getOriginalFilename()));
        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new S3Exception("이미지 업로드에 실패했습니다.");
        }
    }

    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename;
        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentDisposition("inline");
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try{
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata);
            amazonS3.putObject(putObjectRequest);
        }catch (Exception e){
            throw new S3Exception("s3에 이미지 입력을 실패했습니다.");
        }finally {
            byteArrayInputStream.close();
            is.close();
        }
        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    @Override
    public void deleteImage(String imageAddress){
        String key = getKeyFromImageAddress(imageAddress);
        try{
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        }catch (Exception e){
            throw new S3Exception("삭제에 실패했습니다.");
        }
    }

    private String getKeyFromImageAddress(String imageAddress){
        try{
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            log.info(decodingKey);
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        }catch (MalformedURLException | UnsupportedEncodingException e){
            throw new S3Exception("삭제에 실패했습니다.");
        }
    }

    @Override
    public void updateImage(TicketBook ticketBook, List<String> urls) {
        List<Photo> photos = photoRepository.findAllByTicketBook(ticketBook);
        Set<Photo> photosToDelete = new HashSet<>();
        Set<String> urlsToAdd = new HashSet<>(urls);

        photos.forEach(photo -> {
            String url = photo.getUrl();
            if(!urlsToAdd.contains(url)) {
                photosToDelete.add(photo);
            } else {
                urlsToAdd.remove(url);
            }
        });

        if(!photosToDelete.isEmpty()) {
            photoRepository.deleteAll(photosToDelete);
        }

        List<Photo> photosToAdd = urlsToAdd.stream().filter(url -> !url.isBlank())
                .map(url -> new Photo(url, ticketBook))
                .toList();

        if(!photosToAdd.isEmpty()) {
            photoRepository.saveAll(photosToAdd);
        }
    }

    @Override
    public void deleteImages(TicketBook ticketBook) {
        photoRepository.findAllByTicketBook(ticketBook).forEach(photo -> deleteImage(photo.getUrl()));
    }

    @Override
    public List<Photo> getImagesByTicketBook(TicketBook ticketBook) {
        return photoRepository.findAllByTicketBook(ticketBook);
    }
}
