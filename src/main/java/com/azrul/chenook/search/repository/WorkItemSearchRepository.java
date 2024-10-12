/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.search.repository; //Must be outside of @EnableJpaRepositories(basePackages =...). If not Spring Data will expect certain methods

import com.azrul.chenook.domain.WorkItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.CountQuery;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author azrul
 */
//@NoRepositoryBean
public interface WorkItemSearchRepository<T extends WorkItem> extends ElasticsearchRepository<T, Long> {

    @Query(value = """
            {
                "bool": {
                   "must": [
                       {
                            "simple_query_string": {
                              "query": "sme"
                            }
                       },
                       {
                            "match": { "creator":"yuser111"}
                       }]
                }
            }       
    """)
    public Page<T> findByCreator(String searchTerm, String username, Pageable page);

    @CountQuery(value = """
        {
            "bool": {
               "must": [
                   {
                        "simple_query_string": {
                          "query": "?0"
                        }
                   },
                   {
                        "match": { "creator":"?1"}
                   }]
            }
        }                
    """)
    public Long countByCreator(String searchTerm, String username);

    @Query(value = """
    {
        "bool": {
          "must": [
          {
               "simple_query_string": {
                 "query": "#{#searchTerm}"
               }
          },
          {
           "bool":{
               "should": [
                    { "match": { "owners.username": "#{#name}" }},
                    { "bool": {"must":[
                        { "match":{"apprrovals.username": "#{#name}" }},
                        { "exists":{"field": "approved"}}
                    ]} }
            ]}
          }]
        }
    }
    """)
    public Page<T> findAllWhereOwnersOrUndecidedApprovalsContains(String searchTerm, String username, Pageable page);

    @CountQuery(value = """
    {
        "bool": {
              "must": [
              {
                   "simple_query_string": {
                     "query": "#{#searchTerm}"
                   }
              },
              {
               "bool":{
                   "should": [
                        { "match": { "owners.username": "#{#name}" }},
                        { "bool": {"must":[
                            { "match":{"apprrovals.username": "#{#name}" }},
                            { "exists":{"field": "approved"}}
                        ]} }
                ]}
              }]
        }
    }
    """)
    public Long countWhereOwnersOrUndecidedApprovalsContains(String searchTerm, String username);

//    @Query(value="query:{}", nativeQuery = true)
//    public Page<WorkItem> findByCreator(String searchTerm, String username, Pageable page);
//    
//    @Query(value="query:{}", nativeQuery = true)
//    public Long countByCreator(String searchTerm, String username);
//    
//    @Query(value="query:{}", nativeQuery = true)
//    public Page<WorkItem> findAllWhereOwnersOrUndecidedApprovalsContains(String searchTerm, String username, Pageable page);
//    
//    @Query(value="query:{}", nativeQuery = true)
//    public Long countWhereOwnersOrUndecidedApprovalsContains(String searchTerm,String username);
//    @Query(value = """
//    "query": {
//            "bool": {
//              "must": [
//                  {
//                       "simple_query_string": {
//                         "query": "#{#searchTerm}"
//                       }
//                  },
//                  {
//                       "match": { "creator":"#{#name}"}
//                  }]
//            }
//          }
//        }
//    """, nativeQuery = true)
//    public Page<T> findByCreator(String searchTerm, String username, Pageable page);
//
//    @Query(value = """
//    "query": {
//            "bool": {
//              "must": [
//                  {
//                       "simple_query_string": {
//                         "query": "#{#searchTerm}"
//                       }
//                  },
//                  {
//                       "match": { "creator":"#{#name}"}
//                  }]
//            }
//          }
//        }
//    """, nativeQuery = true)
//    public Long countByCreator(String searchTerm, String username);
//
//    @Query(value = """
//     {
//       "query": {
//                "bool": {
//                  "must": [
//                  {
//                       "simple_query_string": {
//                         "query": "#{#searchTerm}"
//                       }
//                  },
//                  {
//                   "bool":{
//                       "should": [
//                            { "match": { "owners.username": "#{#name}" }},
//                            { "bool": {"must":[
//                                { "match":{"apprrovals.username": "#{#name}" }},
//                                { "exists":{"field": "approved"}}
//                            ]} }
//                    ]}
//                  }]
//                }
//              }
//            }
//    """, nativeQuery = true)
//    public Page<T> findAllWhereOwnersOrUndecidedApprovalsContains(String searchTerm, String username, Pageable page);
//
//    @Query(value = """
//     {
//       "query": {
//                "bool": {
//                  "must": [
//                  {
//                       "simple_query_string": {
//                         "query": "#{#searchTerm}"
//                       }
//                  },
//                  {
//                   "bool":{
//                       "should": [
//                            { "match": { "owners.username": "#{#name}" }},
//                            { "bool": {"must":[
//                                { "match":{"apprrovals.username": "#{#name}" }},
//                                { "exists":{"field": "approved"}}
//                            ]} }
//                    ]}
//                  }]
//                }
//              }
//            }
//    """, nativeQuery = true)
//    public Long countWhereOwnersOrUndecidedApprovalsContains(String searchTerm,String username);
}
