package com.azrul.chenook.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.CountQuery;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.azrul.chenook.domain.Reference;
import java.util.Set;

public interface ReferenceSearchRepository<R extends Reference> extends ElasticsearchRepository<R, Long> {

    @Query(value = """
                {
                    "bool": {
                        "must": [
                            {
                                 "simple_query_string": {
                                   "query": "?0"
                                 }
                            },
                            {
                                "bool": {
                                    "filter" : {
                                        "terms" : {
                                          "status" : ["CONFIRMED"]
                                        }
                                    }
                                }
                            }
                        ]
                     }
                }       
        """)
    public Page<R> findConfirmed(String searchTerm, Pageable page);

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
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["CONFIRMED"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Long countConfirmed(String searchTerm);

    @Query(value = """
            {
                "bool": {
                    "must": [
                        {
                             "simple_query_string": {
                               "query": "?0"
                             }
                        },
                        {
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["DEPRECATED", "CONFIRMED"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Page<R> findActive(String searchTerm, Pageable page);

    @Query(value = """
            {
                "bool": {
                    "must": 
                        {
                             "simple_query_string": {
                               "query": "?0"
                             }
                        }
                    
                 }
            }       
    """)
    public Page<R> findAll(String searchTerm, Pageable page);

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
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["DEPRECATED", "CONFIRMED"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Long countActive(String searchTerm);

    @CountQuery(value = """
            {
                "bool": {
                    "must": 
                        {
                             "simple_query_string": {
                               "query": "?0"
                             }
                        }
                    
                 }
            }       
    """)
    public Long countAll(String searchTerm);

    @Query(value = """
            {
                "bool": {
                    "must": [
                        {
                             "simple_query_string": {
                               "query": "?0"
                             }
                        },
                        { 
                            "match": { 
                              "refWorkId": ?1 
                            }
                        },
                        {
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["DRAFT"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Page<R> findDraft(String searchTerm, Long refWorkId, Pageable page);
    
    @Query(value = """
            {
                "bool": {
                    "must": [
                        { 
                            "match": { 
                              "refWorkId": ?0
                            }
                        }
                    ]
                 }
            }       
    """)
    public Set<R> findByRefWork(Long refWorkId);

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
                            "match": { 
                              "refWorkId": ?1 
                            }
                        },
                        {
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["DRAFT"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Long countDraft(String searchTerm, Long refWorkId);

    @Query(value = """
            {
                "bool": {
                    "must": [
                        {
                             "simple_query_string": {
                               "query": "?0"
                             }
                        },
                        { 
                            "match": { 
                              "refWorkId": ?1 
                            }
                       },
                        {
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["DEPRRECATED"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Page<R> findDeprecated(String searchTerm, Long refWorkId, Pageable page);

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
                             "match": { 
                               "refWorkId": ?1 
                             }
                        },
                        {
                            "bool": {
                                "filter" : {
                                    "terms" : {
                                      "status" : ["DEPRECATED"]
                                    }
                                }
                            }
                        }
                    ]
                 }
            }       
    """)
    public Long countDeprecated(String searchTerm, Long refWorkId);

    @Query(value = """
            {
                "bool": {
                    "must": [
                        {
                             "simple_query_string": {
                               "query": "?0"
                             }
                        },
                        { 
                            "match": { 
                              "refWorkId": ?1 
                            }
                        }
                    ]
                 }
            }       
    """)
    public Page<R> find(String searchTerm, Long refWorkId, Pageable page);

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
                            "match": { 
                              "refWorkId": ?1 
                            }
                        }
                    ]
                 }
            }       
    """)
    public Long count(String searchTerm, Long refWorkId);

    @Query(value = """
]               {
                   "bool": {
                        "must": [
                            {"term" : { "refWorkId": ?1}}
                        ]
                     }
                }
    '
    """)
    public Page<R> find(Long refWorkId, Pageable page);

    @CountQuery(value = """
]               {
                   "bool": {
                        "must": [
                            {"term" : { "refWorkId": ?1}}
                        ]
                     }
                }
    '
    """)
    public Long count(Long refWorkId);

}
