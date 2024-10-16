package health.ere.ps.service.cetp.codec;

import de.health.service.cetp.codec.CETPEventDecoderFactory;
import de.servicehealth.config.api.IUserConfigurations;
import io.netty.channel.ChannelInboundHandler;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CETPDecoderFactory implements CETPEventDecoderFactory {

    @Override
    public ChannelInboundHandler build(IUserConfigurations userConfigurations) {
        return new CETPDecoder(userConfigurations);
    }
}
