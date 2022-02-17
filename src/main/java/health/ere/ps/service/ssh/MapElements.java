package health.ere.ps.service.ssh;

import javax.xml.bind.annotation.XmlElement;

class MapElements
{
  @XmlElement public String  key;
  @XmlElement public SSHConnection value;

  private MapElements() {} //Required by JAXB

  public MapElements(String key, SSHConnection value)
  {
    this.key   = key;
    this.value = value;
  }
}
