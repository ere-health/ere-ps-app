package health.ere.ps.service.cetp.event;

import de.gematik.ws.conn.eventservice.v7.Event;
import de.gematik.ws.conn.eventservice.v7.EventSeverityType;
import de.gematik.ws.conn.eventservice.v7.EventType;
import de.health.service.cetp.domain.eventservice.event.CetpEvent;
import health.ere.ps.profile.RUDevTestProfile;
import health.ere.ps.service.cetp.mapper.event.EventMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(RUDevTestProfile.class)
public class EventMapperTest {

    @Inject
    EventMapper eventMapper;

    @Test
    public void soapEventMappedCorrectly() {
        Event event = new Event();
        event.setTopic("CARD/INSERTED");
        event.setType(EventType.OPERATION);
        event.setSeverity(EventSeverityType.INFO);
        event.setSubscriptionID("subscriptionID");

        Event.Message message = new Event.Message();
        Event.Message.Parameter p1 = new Event.Message.Parameter();
        p1.setKey("Key1");
        p1.setValue("Value1");
        message.getParameter().add(p1);
        Event.Message.Parameter p2 = new Event.Message.Parameter();
        p2.setKey("Key2");
        p2.setValue("Value2");
        message.getParameter().add(p2);

        event.setMessage(message);

        CetpEvent domain = eventMapper.toDomain(event);
        assertEquals(event.getTopic(), domain.getTopic());
        assertEquals(event.getType().value(), domain.getType().getValue());
        assertEquals(event.getSeverity().value(), domain.getSeverity().getValue());
        assertEquals(event.getSubscriptionID(), domain.getSubscriptionId());

        List<Event.Message.Parameter> parameter = event.getMessage().getParameter();
        assertEquals(parameter.size(), domain.getParameters().size());
        assertParameter(event, domain, 0);
        assertParameter(event, domain, 1);
    }

    private void assertParameter(Event event, CetpEvent domain, int index) {
        List<Event.Message.Parameter> parameter = event.getMessage().getParameter();
        assertEquals(parameter.get(index).getKey(), domain.getParameters().get(index).getKey());
        assertEquals(parameter.get(index).getValue(), domain.getParameters().get(index).getValue());
    }
}
