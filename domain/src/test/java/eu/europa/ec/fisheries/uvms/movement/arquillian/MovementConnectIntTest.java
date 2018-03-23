package eu.europa.ec.fisheries.uvms.movement.arquillian;


import eu.europa.ec.fisheries.uvms.movement.entity.MovementConnect;
import eu.europa.ec.fisheries.uvms.movement.util.DateUtil;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Created by thofan on 2017-02-14.
 */

@RunWith(Arquillian.class)
public class MovementConnectIntTest extends TransactionalTests {

    @Test
    @OperateOnDeployment("normal")
    public void createMovementConnect() {

        for(int i = 0 ; i < 10 ; i++) {
            MovementConnect movementConnect = new MovementConnect();
            movementConnect.setValue(UUID.randomUUID().toString());
            movementConnect.setUpdatedBy("arquillian");
            movementConnect.setUpdated(DateUtil.nowUTC());
            em.persist(movementConnect);
            em.flush();
        }
    }
}
