package com.azrul.smefinancing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azrul.chenook.repository.ReferenceRepository;
import com.azrul.chenook.search.repository.ReferenceSearchRepository;
import com.azrul.chenook.service.ReferenceService;
import com.azrul.smefinancing.domain.Location;

@Service
public class LocationService extends ReferenceService<Location> {

    private ReferenceRepository<Location> refRepo;
    private ReferenceSearchRepository<Location> refSearchRepo;

    public LocationService(
        @Autowired ReferenceRepository<Location> refRepo, 
        @Autowired ReferenceSearchRepository<Location> refSearchRepo
    ) {
        this.refRepo = refRepo;
        this.refSearchRepo = refSearchRepo;
    }

    @Override
    protected ReferenceRepository<Location> getRefRepo() {
       return refRepo;
    }

    @Override
    protected ReferenceSearchRepository<Location> getRefSearchRepo() {
        return refSearchRepo;  
    }
    
}