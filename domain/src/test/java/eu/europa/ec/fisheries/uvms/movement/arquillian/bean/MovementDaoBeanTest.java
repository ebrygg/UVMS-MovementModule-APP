package eu.europa.ec.fisheries.uvms.movement.arquillian.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;

import eu.europa.ec.fisheries.uvms.movement.exception.MovementDomainException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.schema.movement.v1.SegmentCategoryType;
import eu.europa.ec.fisheries.uvms.movement.arquillian.TransactionalTests;
import eu.europa.ec.fisheries.uvms.movement.arquillian.bean.util.MovementHelpers;
import eu.europa.ec.fisheries.uvms.movement.bean.IncomingMovementBean;
import eu.europa.ec.fisheries.uvms.movement.bean.MovementBatchModelBean;
import eu.europa.ec.fisheries.uvms.movement.dao.AreaDao;
import eu.europa.ec.fisheries.uvms.movement.dao.MovementDao;
import eu.europa.ec.fisheries.uvms.movement.dao.bean.MovementDaoBean;
import eu.europa.ec.fisheries.uvms.movement.entity.Movement;
import eu.europa.ec.fisheries.uvms.movement.entity.area.Area;
import eu.europa.ec.fisheries.uvms.movement.entity.area.AreaType;

@RunWith(Arquillian.class)
public class MovementDaoBeanTest extends TransactionalTests {
	
	@EJB
    private MovementBatchModelBean movementBatchModelBean;

    @EJB
    private MovementDao movementDao;

    @EJB
    private IncomingMovementBean incomingMovementBean;
	
	@EJB
	MovementDaoBean movementDaoBean;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testGetMovementsByGUID() throws MovementDomainException {
		Movement output = movementDaoBean.getMovementByGUID("");
		assertNull(output);
		
		String connectId = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectId, "TEST", Instant.now());
		move.setGuid();
		incomingMovementBean.processMovement(move);
		em.flush();
		
		output = movementDaoBean.getMovementByGUID(move.getGuid());
		System.out.println(output);
		assertEquals(move.getGuid(), output.getGuid());
	}
	
	@Test
	public void testGetLatestMovementByConnectIdList() throws MovementDomainException {
		String connectID = UUID.randomUUID().toString();
		String connectID2 = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move1 = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, 0, SegmentCategoryType.OTHER, connectID, "TEST", Instant.now().plusSeconds(1));
		Movement move3 = movementHelpers.createMovement(22D, 22D, 0, SegmentCategoryType.OTHER, connectID2, "TEST", Instant.now().plusSeconds(2));
		move1.setGuid();
		incomingMovementBean.processMovement(move1);
		move2.setGuid();
		incomingMovementBean.processMovement(move2);
		move3.setGuid();
		incomingMovementBean.processMovement(move3);
		em.flush();
		
		List<String> input = new ArrayList<>();
		input.add(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertEquals(1, output.size());
		
		//add the same id again just to see if we get duplicates
		input.add(connectID);
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertEquals(1, output.size());
		assertEquals(move2.getGuid(), output.get(0).getGuid());
		
		input.add(connectID2);
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertEquals(2, output.size());
		
		//null as input should return an empty set
		output = movementDaoBean.getLatestMovementsByConnectIdList(null);
		assertTrue(output.isEmpty());
			
		//random input should result in an empty set
		input = new ArrayList<>();
		input.add(UUID.randomUUID().toString());
		output = movementDaoBean.getLatestMovementsByConnectIdList(input);
		assertTrue(output.isEmpty());
	}
	
	@Test
	public void testGetLatestMovementsByConnectID() throws MovementDomainException {
		String connectID = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move1 = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, 0, SegmentCategoryType.OTHER, connectID, "TEST", Instant.now().plusSeconds(1));
		Movement move3 = movementHelpers.createMovement(22D, 22D, 0, SegmentCategoryType.OTHER, connectID, "TEST42", Instant.now().plusSeconds(2));
		move1.setGuid();
		incomingMovementBean.processMovement(move1);
		move2.setGuid();
		incomingMovementBean.processMovement(move2);
		move3.setGuid();
		incomingMovementBean.processMovement(move3);
		em.flush();
		
		System.out.println(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());
		
		output = movementDaoBean.getLatestMovementsByConnectId(connectID, 3);
		assertEquals(3, output.size());
		
//		try {
//			output = movementDaoBean.getLatestMovementsByConnectId(connectID, -3);
//			fail("negative value as input should result in an exception");
//		} catch (MovementDomainRuntimeException e) {
//			assertTrue(true);
//		}
		//should result in a no result output akka null
		output = movementDaoBean.getLatestMovementsByConnectId("0", 1);
		assertNull(output);
		
		//funnily enough this is only true if you are only expecting 1 result.......
		output = movementDaoBean.getLatestMovementsByConnectId("0", 2);
		assertTrue(output.isEmpty());
	}

	@Test
	public void testGetLatestMovementsByConnectID_willFail() throws MovementDomainException {

		thrown.expect(EJBTransactionRolledbackException.class);

		String connectID = UUID.randomUUID().toString();
		MovementHelpers movementHelpers = new MovementHelpers(em, movementBatchModelBean, movementDao);
		Movement move1 = movementHelpers.createMovement(20D, 20D, 0, SegmentCategoryType.OTHER, connectID, "TEST", Instant.now());
		Movement move2 = movementHelpers.createMovement(21D, 21D, 0, SegmentCategoryType.OTHER, connectID, "TEST", Instant.ofEpochMilli(System.currentTimeMillis() + 100L));
		Movement move3 = movementHelpers.createMovement(22D, 22D, 0, SegmentCategoryType.OTHER, connectID, "TEST42", Instant.ofEpochMilli(System.currentTimeMillis() + 200L));
		move1.setGuid();
		incomingMovementBean.processMovement(move1);
		move2.setGuid();
		incomingMovementBean.processMovement(move2);
		move3.setGuid();
		incomingMovementBean.processMovement(move3);
		em.flush();

		System.out.println(connectID);
		List<Movement> output = movementDaoBean.getLatestMovementsByConnectId(connectID, 1);
		assertEquals(1, output.size());
		assertEquals(move3.getGuid(), output.get(0).getGuid());

		movementDaoBean.getLatestMovementsByConnectId(connectID, -3);
	}
	
	@Test
	public void testGetAreaTypeByCode() {
		AreaType areaType = createAreaType();
		em.persist(areaType);
		em.flush();
		
		AreaType output = movementDaoBean.getAreaTypeByCode("TestAreaType");
		assertEquals(areaType, output);
		
		output = movementDaoBean.getAreaTypeByCode("TestAreaType2");// should result in a null return
		assertNull(output);
		
		try {
			//trying to create a duplicate
			AreaType areaTypeDuplicate = createAreaType();
			em.persist(areaTypeDuplicate);
			em.flush();
			fail("duplicate namnes should not be allowed"); //thus the catch clause for multiple areas in the method is invalid
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@EJB
    private AreaDao areaDao;
	
	@Test
	public void testGetAreaByRemoteIDAndCode() throws MovementDomainException {
		AreaType areaType = createAreaType();
		em.persist(areaType);
		em.flush();

		Area area = createArea(areaType);
		
		Area createdArea = areaDao.createMovementArea(area);
        areaDao.flushMovementAreas();

		Area output = movementDaoBean.getAreaByRemoteIdAndCode(areaType.getName(), null); //remoteId is not used at all  TestAreaCode
		assertEquals(area.getAreaId(), output.getAreaId());
		
		output = movementDaoBean.getAreaByRemoteIdAndCode("ShouldNotExist", null);
		assertNull(output);
	}

	@Test
	public void testGetAreaByRemoteIDAndCode_willFail() throws MovementDomainException {

		thrown.expect(EJBTransactionRolledbackException.class);
		thrown.expectMessage("No valid input parameters to method getAreaByRemoteIdAndCode");

		AreaType areaType = createAreaType();

		em.persist(areaType);
		em.flush();

		Area area = createArea(areaType);

		areaDao.createMovementArea(area);
		areaDao.flushMovementAreas();

		movementDaoBean.getAreaByRemoteIdAndCode(null, null);
	}
	
	@Test
	public void testIsDateAlreadyInserted() {
		//only testing the no result part since the rest of teh function is tested elsewhere
		List<Movement> output = movementDaoBean.isDateAlreadyInserted("ShouldNotExist", Instant.now());
		assertTrue(output.isEmpty());
	}

	private AreaType createAreaType() {
		AreaType areaType = new AreaType();
		String input = "TestAreaType";
		areaType.setName(input);
		areaType.setUpdatedTime(Instant.now());
		areaType.setUpdatedUser("TestUser");
		return areaType;
	}

	private Area createArea(AreaType areaType) {
		Area area = new Area();
		area.setAreaName("TestArea");
		area.setAreaCode(areaType.getName());
		area.setRemoteId("TestRemoteId");
		area.setAreaUpdattim(Instant.now());
		area.setAreaUpuser("TestUser");
		area.setAreaType(areaType);
		return area;
	}
}
