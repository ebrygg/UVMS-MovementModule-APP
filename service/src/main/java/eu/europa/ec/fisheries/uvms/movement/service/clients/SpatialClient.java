/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.movement.service.clients;

import java.util.List;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.service.entity.Movement;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRS;

public interface SpatialClient {
    SpatialEnrichmentRS getEnrichment(Point location);
    BatchSpatialEnrichmentRS getBatchEnrichment(List<Point> locations);
    SegmentCategoryType getSegmentCategoryType(Movement movement1, Movement movement2);
}
