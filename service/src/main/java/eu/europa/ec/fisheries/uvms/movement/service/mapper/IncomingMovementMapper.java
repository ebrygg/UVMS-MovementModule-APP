package eu.europa.ec.fisheries.uvms.movement.service.mapper;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetMTEnrichmentResponse;
import eu.europa.ec.fisheries.uvms.movement.service.dto.SegmentCalculations;
import eu.europa.ec.fisheries.uvms.movement.service.entity.IncomingMovement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.service.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.service.util.CalculationUtil;
import eu.europa.ec.fisheries.uvms.movementrules.model.dto.MovementDetails;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

public abstract class IncomingMovementMapper {


    public static Movement mapNewMovementEntity(IncomingMovement ic, String username) {
        Movement entity = new Movement();

        if (ic.getReportedSpeed() != null) {
            entity.setSpeed(ic.getReportedSpeed().floatValue());
        }
        entity.setHeading( ic.getReportedCourse() == null ? -1f : ic.getReportedCourse().floatValue());
        entity.setInternalReferenceNumber(ic.getInternalReferenceNumber());
        entity.setTripNumber(ic.getTripNumber());
        entity.setStatus(ic.getStatus());
        entity.setLesReportTime(ic.getLesReportTime());
        entity.setSourceSatelliteId(ic.getSourceSatelliteId());

        Coordinate coordinate = new Coordinate(ic.getLongitude(), ic.getLatitude());
        GeometryFactory factory = new GeometryFactory();
        Point point = factory.createPoint(coordinate);
        point.setSRID(4326);
        entity.setLocation(point);

        entity.setUpdated(Instant.now());
        entity.setUpdatedBy(username);

        if (ic.getMovementSourceType() != null) {
            entity.setMovementSource(MovementSourceType.fromValue(ic.getMovementSourceType()));
        } else {
            entity.setMovementSource(MovementSourceType.OTHER);
        }

        entity.setMovementType(MovementTypeType.fromValue(ic.getMovementType()));

        if (ic.getPositionTime() != null) {
            entity.setTimestamp(ic.getPositionTime());
        } else {
            entity.setTimestamp(Instant.now());
        }

        return entity;
    }

    public static MovementConnect mapNewMovementConnect(IncomingMovement ic, String username) {
        //MovementConnect (aka asset)
        MovementConnect movementConnect = new MovementConnect();
        movementConnect.setId(UUID.fromString(ic.getAssetGuid()));
        movementConnect.setFlagState(ic.getFlagState());
        movementConnect.setName(ic.getAssetName());
        movementConnect.setUpdated(Instant.now());
        movementConnect.setUpdatedBy(username);
        return movementConnect;
    }

    public static MovementDetails mapMovementDetails(IncomingMovement im, Movement movement, AssetMTEnrichmentResponse response) {
        MovementDetails md = new MovementDetails();
        md.setMovementGuid(movement.getId().toString());
        md.setLongitude(movement.getLocation().getX());
        md.setLatitude(movement.getLocation().getY());
        md.setMovementType(movement.getMovementType().value());
        Movement previousMovement = movement.getPreviousMovement();
        if(previousMovement != null) {
            SegmentCalculations positionCalculations = CalculationUtil.getPositionCalculations(previousMovement, movement);
            md.setCalculatedCourse(positionCalculations.getCourse());
            md.setCalculatedSpeed(positionCalculations.getAvgSpeed());
            //md.setSegmentType(movement.getFromSegment().getSegmentCategory().value()); // TODO
        }
        md.setReportedCourse(movement.getHeading() != null ? (double)movement.getHeading() : null);
        md.setReportedSpeed(movement.getSpeed() != null ? (double)movement.getSpeed() : null);
        md.setPositionTime(movement.getTimestamp());
        md.setStatusCode(movement.getStatus());
        md.setTripNumber(movement.getTripNumber());
        md.setWkt("");
        md.setInternalReferenceNumber(movement.getInternalReferenceNumber());

        // Asset
        md.setAssetGuid(response.getAssetUUID());
        md.setAssetIdGearType(response.getGearType());
        md.setExternalMarking(response.getExternalMarking());
        md.setFlagState(response.getFlagstate());
        md.setCfr(response.getCfr());
        md.setIrcs(response.getIrcs());
        md.setAssetName(response.getAssetName());
        md.setAssetStatus(response.getAssetStatus());
        md.setMmsi(response.getMmsi());
        md.setImo(response.getImo());
        md.setAssetGroups(response.getAssetGroupList());
        md.setConnectId(response.getAssetUUID());
        md.setAssetType(response.getVesselType());

        // MobileTerminal
        md.setChannelGuid(response.getChannelGuid());
        md.setMobileTerminalGuid(response.getMobileTerminalGuid());
        md.setComChannelType(im.getComChannelType());
        md.setMobileTerminalType(response.getMobileTerminalType());
        md.setMobileTerminalDnid(response.getDNID());
        md.setMobileTerminalMemberNumber(response.getMemberNumber());
        md.setMobileTerminalSerialNumber(response.getSerialNumber());
        //TODO: missing
        //md.setMobileTerminalStatus();
        md.setSource(movement.getMovementSource().value());

        if (previousMovement != null) {
            md.setPreviousLatitude(previousMovement.getLocation().getY());
            md.setPreviousLongitude(previousMovement.getLocation().getX());
        }
        
        /*
    private List<String> vicinityOf;
         */

        return md;
    }
}
