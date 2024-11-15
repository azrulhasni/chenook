/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.service;

//import com.azrul.smefinancing.domain.Applicant;
import com.azrul.chenook.domain.Attachment;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.repository.AttachmentRepository;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Service
public class AttachmentService<T extends WorkItem> {
    private final MinioClient minioClient;
    private final String minioBucket;
    private final AttachmentRepository attachmentRepo;

    
    public AttachmentService(
            @Autowired AttachmentRepository attachmentRepo,
            @Autowired MinioClient minioClient,
            @Value("${minio.bucket}") String minioBucket
    ){
        this.minioClient=minioClient;
        this.minioBucket = minioBucket;
        this.attachmentRepo=attachmentRepo;
    }

//    @Transactional
//    private Attachment save(Attachment attc){
//        return this.attachmentRepo.save(attc);
//    }
//    
//    @Transactional
//    public void remove(Attachment attc){
//        this.attachmentRepo.delete(attc); 
//    }
//    
//    public CallbackDataProvider.FetchCallback<Attachment, Void> findAttachmentsByParentAndContext(Long parentId, String context) {
//        return query -> {
//            var vaadinSortOrders = query.getSortOrders();
//            var springSortOrders = new ArrayList<Sort.Order>();
//            for (QuerySortOrder so : vaadinSortOrders) {
//                String colKey = so.getSorted();
//                if (so.getDirection() == SortDirection.ASCENDING) {
//                    springSortOrders.add(Sort.Order.asc(colKey));
//                }
//            }
//            return this.attachmentRepo.findByParentIdAndContext(
//                    parentId,
//                    context,
//                    PageRequest.of(
//                            query.getPage(),
//                            query.getPageSize(),
//                            Sort.by(springSortOrders)
//                    )).stream();
//        };
//    }
//    

 
    @Transactional
    public Attachment saveToStorage(
            String fileLocation,
            String fileName,
            String mimeType,
            byte[] content) {

        try {

            var response = minioClient.putObject(PutObjectArgs.builder().bucket(minioBucket).object(fileLocation+"/"+fileName).stream(
                    new ByteArrayInputStream(content), content.length, -1).build());
            
            if (response.etag()==null){
                return null;
            }else{
                Attachment attachment = new Attachment();
                attachment.setFileLocation(fileLocation);
                attachment.setFileName(fileName);
                attachment.setMimeType(mimeType);
                attachment.setSize((long)content.length);
                
                
               return attachmentRepo.save(attachment);
            }

        } catch (ErrorResponseException
                | InsufficientDataException
                | InternalException
                | InvalidKeyException
                | InvalidResponseException
                | IOException
                | NoSuchAlgorithmException
                | ServerException
                | XmlParserException ex) {
            LogFactory.getLog(AttachmentService.class.getName()).fatal("Error", ex);
            return null;
        }

    }

    public void getFromStorage(String fileLocation, OutputStream out) {
        try {
            minioClient.getObject(
                    GetObjectArgs
                            .builder()
                            .bucket(minioBucket)
                            .object(fileLocation)
                            .build()
            ).transferTo(out);

        } catch (ErrorResponseException
                | InsufficientDataException
                | InternalException
                | InvalidKeyException
                | InvalidResponseException
                | IOException
                | NoSuchAlgorithmException
                | ServerException
                | XmlParserException ex) {

            LogFactory.getLog(AttachmentService.class.getName()).fatal("Error", ex);
        }
    }
    

}
