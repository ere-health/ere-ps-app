package health.ere.ps.service.ssh;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import health.ere.ps.config.AppConfig;
import health.ere.ps.event.NewWebsocketEvent;
import health.ere.ps.event.SSHConnectionOfferingEvent;

@ApplicationScoped
public class SSHTunnelManager {
    
    static String SSH_CONNECTIONS_XML_FILE = "ssh-connections.xml";

    private static Logger log = Logger.getLogger(SSHTunnelManager.class.getName());

    @Inject
    AppConfig appConfig;

    @Inject
    Event<SSHConnectionOfferingEvent> sSHConnectionOfferingEvent;

    static JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(SSHConnections.class);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not load jaxb context", e);
        }
    }

    SSHConnections sshConnections = new SSHConnections();
    
    Cache<String, SSHConnectionOfferingEvent> openSshConnectionOfferings = CacheBuilder.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(30, TimeUnit.MINUTES)
    .build();
    
    Integer minFreePort = 1051;

    @PostConstruct
    void init() {
        // load ssh connections
        File file = getSSHConnectionsFile();
        if(file.exists()) {
            loadSSHConnections(file);
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Could not create "+SSH_CONNECTIONS_XML_FILE, e);
            }
        }
    }

    File getSSHConnectionsFile() {
        File file = new File(SSH_CONNECTIONS_XML_FILE);
        return file;
    }

    void loadSSHConnections(File file) {
        try {
            sshConnections = (SSHConnections) context.createUnmarshaller().unmarshal(file);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not load "+SSH_CONNECTIONS_XML_FILE, e);
        }
    }

    public SSHConnectionOfferingEvent getNextSSHConnectionOffering(Session session) {
        // create an SSH Connection Offering with the next free ports bigger than minFreePort not occupied by sshConnections or openSshConnectionOfferings
        int firstPort = minFreePort;
        String user = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();
        while(!isPortAvailable(firstPort)) {
            firstPort++;
        }
        SSHConnectionOfferingEvent sSHConnectionOfferingEvent = new SSHConnectionOfferingEvent();
        sSHConnectionOfferingEvent.setSession(session);
        sSHConnectionOfferingEvent.setUser(user);
        sSHConnectionOfferingEvent.setSecret(secret);
        if(appConfig != null) {
            sSHConnectionOfferingEvent.setIdpBaseURL(appConfig.getIdpBaseURL());
            sSHConnectionOfferingEvent.setIdpAuthRequestRedirectURL(appConfig.getIdpAuthRequestRedirectURL());
            sSHConnectionOfferingEvent.setIdpClientId(appConfig.getIdpClientId());
            sSHConnectionOfferingEvent.setPrescriptionServiceURL(appConfig.getPrescriptionServiceURL());
        }
        sSHConnectionOfferingEvent.setPort(SSHService.PORT);
        sSHConnectionOfferingEvent.getPorts().add(firstPort);
        sSHConnectionOfferingEvent.getPorts().add(firstPort+1);
        sSHConnectionOfferingEvent.getPorts().add(firstPort+2);

        openSshConnectionOfferings.put(user,sSHConnectionOfferingEvent);
        return sSHConnectionOfferingEvent;
    }

    public boolean isPortAvailable(Integer port) {
        for(Entry<String, SSHConnection> sshConnectionEntry : sshConnections.getSshConnection().entrySet()) {
            if(sshConnectionEntry.getValue().getPorts().contains(port)) {
                return false;
            }
        }
        for(Entry<String, SSHConnectionOfferingEvent> sshConnectionOfferingEntry : openSshConnectionOfferings.asMap().entrySet()) {
            if(sshConnectionOfferingEntry.getValue().getPorts().contains(port)) {
                return false;
            }
        }
        return true;
    }

    public boolean acceptSSHConnection(String user, String secret) {
        // check that user and password ar in sshConnection or in openSshConnectionOfferings
        // if in openSshConnectionOfferings than move entry into sshConnection and persist sshConnections

        if(openSshConnectionOfferings.asMap().containsKey(user)) {
            SSHConnectionOfferingEvent sSHConnectionOfferingEvent = openSshConnectionOfferings.asMap().get(user);
            if(sSHConnectionOfferingEvent.getSecret().equals(secret)) {
                openSshConnectionOfferings.asMap().remove(user);
                SSHConnection sshConnection = new SSHConnection(sSHConnectionOfferingEvent);
                sshConnections.getSshConnection().put(user, sshConnection);
                persistSSHConnections();
                return true;
            } else {
                return false;
            }
        } else if(sshConnections.getSshConnection().containsKey(user)) {
            return sshConnections.getSshConnection().get(user).getSecret().equals(secret);
        }

        return false;
    }

    public void denySSHConnectionOffering(String user) {
        openSshConnectionOfferings.asMap().remove(user);
    }

    public void persistSSHConnections() {
        File file = getSSHConnectionsFile();
        try {
            context.createMarshaller().marshal(sshConnections, file);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not persist to "+SSH_CONNECTIONS_XML_FILE, e);
        }
    }

    public void onNewWebsocket(@ObservesAsync NewWebsocketEvent newWebsocketEvent) {
        sSHConnectionOfferingEvent.fireAsync(getNextSSHConnectionOffering(newWebsocketEvent.getSession()));
    } 
}
