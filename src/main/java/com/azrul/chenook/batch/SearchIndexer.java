/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.batch;

import com.azrul.chenook.domain.WorkItem;
import com.azrul.smefinancing.search.repository.FinApplicationSearchRepository;
//import com.azrul.smefinancing.search.repository.FinApplicationSearchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author azrul
 */
@Component
public class SearchIndexer {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    FinApplicationSearchRepository wiSearchRepo;
    
     @Transactional
     //@EventListener(ApplicationReadyEvent.class)
     public void startIndexing(){
         List<WorkItem> items  =entityManager
                      .createQuery("Select w from WorkItem w", WorkItem.class)
                      .getResultList();
         for (WorkItem item:items){
           //wiSearchRepo.save(item);
         }
//         List<WorkItem> res = wiSearchRepo.findByStatus("IN_PROGRESS");
//         for (WorkItem r:res){
//             System.out.println("id:"+r.getId());
//         }
//         
//         List<WorkItem> res2 = wiSearchRepo.findByOwnersIsEmpty();
//         for (WorkItem r:res2){
//             System.out.println("id:"+r.getId());
//         }
     }

//    @Transactional
//    @EventListener(ApplicationReadyEvent.class)
//    public void startIndexing() {
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<FinApplication> criteriaQuery = criteriaBuilder.createQuery(FinApplication.class);
//            Root<FinApplication> rootQuery = criteriaQuery.from(FinApplication.class);
//            criteriaQuery.select(rootQuery);
//            TypedQuery<FinApplication> query = entityManager.createQuery(criteriaQuery);
//            List<FinApplication> apps = query.getResultList();
//
//            String serverUrl = "http://localhost:9200";
//            String apiKey = "";
//
//            final HttpHost host = new HttpHost("localhost", 9200, "http");
//            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//            //Only for demo purposes. Don't specify your credentials in code.
//            credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials("admin", ""));
//
//            //Initialize the client with SSL and TLS enabled
//            final RestClient restClient = RestClient.builder(host).
//                    setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//                        @Override
//                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//                            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//                        }
//                    }).build();
//
//            final OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
//            final OpenSearchClient client = new OpenSearchClient(transport);
//
//            for (FinApplication app : apps) {
//                IndexResponse response = client.index(i -> i
//                        .index("finapplication-1111")
//                        .id(app.getId().toString())
//                        .document(app)
//                );
//                System.out.println("Saving:" + response.id());
//            }
//            
//            
//
//            for (FinApplication app : apps) {
//                GetResponse<FinApplication> response = client.get(g -> g
//                        .index("finapplication-1111")
//                        .id(app.getId().toString()),
//                        FinApplication.class
//                );
//
//                if (response.found()) {
//                    FinApplication finapp = response.source();
//                    System.out.println("Fin app:" + finapp.getName());
//                }
//            }
//          
//            
//          BoolQuery fquery = QueryBuilders
//                  .bool()
//                  .filter(
//                          QueryBuilders
//                                  .term()
//                                  .field("ownerIsEmpty")
//                                  .value(FieldValue.FALSE)
//                                  .build()
//                                  ._toQuery()
//                  )
//                  .must(s->s.simpleQueryString(q->q.query("6502")))
//                  .build();
//          
//            SearchRequest searchRequest = SearchRequest.of(s -> s
//                    .index("finapplication-1111") // Specify the index
//                    .query(fquery._toQuery())
//            );
//            
//
//            // Execute the search request
//            SearchResponse<FinApplication> searchResponse = client.search(searchRequest, FinApplication.class);
//
//            searchResponse.hits().hits().forEach(hit -> System.out.println("Found document: " + hit.id()));
//            transport.close();
//        } catch (IOException ex) {
//            Logger.getLogger(SearchIndexer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
