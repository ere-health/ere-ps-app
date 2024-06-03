package health.ere.ps.service.health.check;

import health.ere.ps.config.RuntimeConfig;
import health.ere.ps.service.status.StatusService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class StatusCheck implements Check {

    private final StatusService statusService;

    @Inject
    public StatusCheck(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public String getName() {
        return STATUS_CHECK;
    }

    @Override
    public Status getStatus(RuntimeConfig runtimeConfig) {
        try {
            health.ere.ps.model.status.Status status = statusService.getStatus(runtimeConfig);
            boolean ok = status.getCautReadable()
                && status.getConnectorReachable()
                && status.getComfortsignatureAvailable()
                && status.getIdpReachable()
                && status.getEhbaAvailable()
                && status.getFachdienstReachable()
                && status.getSmcbAvailable()
                && status.getIdpaccesstokenObtainable();
            return ok ? Status.Up200 : Status.Down503;
        } catch (Throwable e) {
            return Status.Down500;
        }
    }

    @Override
    public Map<String, String> getData(RuntimeConfig runtimeConfig) {
        health.ere.ps.model.status.Status status = statusService.getStatus(runtimeConfig);
        Map<String, String> map = new HashMap<>();
        map.put("cautReadable", String.valueOf(status.getCautReadable()));
        map.put("cautInformation", status.getCautInformation());
        map.put("comfortsignatureAvailable", String.valueOf(status.getComfortsignatureAvailable()));
        map.put("connectorReachable", String.valueOf(status.getConnectorReachable()));
        map.put("comfortsignatureInformation", status.getComfortsignatureInformation());
        map.put("connectorInformation", status.getConnectorInformation());
        map.put("ehbaAvailable", String.valueOf(status.getEhbaAvailable()));
        map.put("ehbaInformation", status.getEhbaInformation());
        map.put("fachdienstReachable", String.valueOf(status.getFachdienstReachable()));
        map.put("fachdienstInformation", status.getFachdienstInformation());
        map.put("idpReachable", String.valueOf(status.getIdpReachable()));
        map.put("idpInformation", status.getIdpInformation());
        map.put("idpaccesstokenObtainable", String.valueOf(status.getIdpaccesstokenObtainable()));
        map.put("smcbAvailable", String.valueOf(status.getSmcbAvailable()));
        map.put("smcbInformation", status.getSmcbInformation());
        return map;
    }
}
