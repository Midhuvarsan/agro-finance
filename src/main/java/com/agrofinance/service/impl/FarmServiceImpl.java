package com.agrofinance.service.impl;
 
import com.agrofinance.dto.CropRequest;
import com.agrofinance.dto.CropResponse;
import com.agrofinance.dto.LandRequest;
import com.agrofinance.dto.LandResponse;
import com.agrofinance.entity.Crop;
import com.agrofinance.entity.CropType;
import com.agrofinance.entity.Farmer;
import com.agrofinance.entity.Land;
import com.agrofinance.repository.CropRepository;
import com.agrofinance.repository.CropTypeRepository;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.repository.LandRepository;
import com.agrofinance.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {
 
    private final FarmerRepository farmerRepository;
    private final LandRepository landRepository;
    private final CropRepository cropRepository;
    private final CropTypeRepository cropTypeRepository;
 
    @Override
    @Transactional
    public LandResponse addLand(Long farmerUserId, LandRequest request) {
        Farmer farmer = farmerRepository.findById(farmerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complete your profile first"));
 
        Land land = new Land();
        land.setFarmer(farmer);
        land.setSurveyNumber(request.surveyNumber());
        land.setAreaAcres(request.areaAcres());
        land.setLocation(request.location());
        land.setSoilType(request.soilType());
 
        return toLandResponse(landRepository.save(land));
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<LandResponse> listMyLands(Long farmerUserId) {
        return landRepository.findByFarmerUserId(farmerUserId).stream()
                .map(this::toLandResponse)
                .toList();
    }
 
    @Override
    @Transactional
    public CropResponse addCrop(Long farmerUserId, CropRequest request) {
 
        // Ownership enforced IN the query: a crop can only be attached
        // to a land parcel this farmer actually owns. Returning 404 (not
        // 403) for someone else's land also avoids leaking whether that
        // land id exists at all.
        Land land = landRepository.findByIdAndFarmerUserId(request.landId(), farmerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Land not found"));
 
        CropType cropType = cropTypeRepository.findById(request.cropTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown crop type"));
 
        if (request.sowingDate() != null && request.expectedHarvestDate() != null
                && request.expectedHarvestDate().isBefore(request.sowingDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected harvest date cannot be before sowing date");
        }
 
        Crop crop = new Crop();
        crop.setLand(land);
        crop.setCropType(cropType);
        crop.setSeason(request.season());
        crop.setSowingDate(request.sowingDate());
        crop.setExpectedHarvestDate(request.expectedHarvestDate());
 
        return toCropResponse(cropRepository.save(crop));
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CropResponse> listMyCrops(Long farmerUserId) {
        return cropRepository.findAllByFarmerUserId(farmerUserId).stream()
                .map(this::toCropResponse)
                .toList();
    }
 
    private LandResponse toLandResponse(Land land) {
        return new LandResponse(
                land.getId(),
                land.getSurveyNumber(),
                land.getAreaAcres(),
                land.getLocation(),
                land.getSoilType()
        );
    }
 
    private CropResponse toCropResponse(Crop crop) {
        return new CropResponse(
                crop.getId(),
                crop.getLand().getId(),
                crop.getCropType().getName(),
                crop.getSeason(),
                crop.getSowingDate(),
                crop.getExpectedHarvestDate()
        );
    }
 
}
 












