package com.azrul.chenook.search.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.CountQuery;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.azrul.chenook.domain.Reference;

public interface ReferenceSearchRepository<R extends Reference> extends ElasticsearchRepository<R, Long> { 
    @Query(value = """
            {
                "bool": {
                   "must": [
                       {
                            "simple_query_string": {
                              "query": "?0"
                            }
                       }]
                }
            }       
    """)
    public Page<R> find(String searchTerm, Pageable page);

    @CountQuery(value = """
        {
            "bool": {
               "must": [
                   {
                        "simple_query_string": {
                          "query": "?0"
                        }
                   }]
            }
        }                
    """)
    public Long count(String searchTerm);
}