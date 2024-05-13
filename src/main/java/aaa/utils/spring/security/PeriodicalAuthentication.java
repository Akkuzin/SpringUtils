package aaa.utils.spring.security;

import static java.util.Arrays.asList;

import aaa.utils.spring.integration.jpa.AbstractPOJO;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class PeriodicalAuthentication extends AbstractAuthenticationToken {

  public static final String ROLE_PERIODICAL = "ROLE_PERIODICAL";

  public static class Periodical extends AbstractPOJO {

    @Override
    public Long getId() {
      return 0L;
    }

    @Override
    public String getRepresentation() {
      return "Periodical";
    }
  }

  @Override
  public boolean isAuthenticated() {
    return true;
  }

  public static final Periodical PERIODICAL = new Periodical();

  public static final PeriodicalAuthentication PERIODICAL_AUTHENTICATION =
      new PeriodicalAuthentication();

  public PeriodicalAuthentication() {
    super(asList(new SimpleGrantedAuthority(ROLE_PERIODICAL)));
  }

  @Override
  public Object getCredentials() {
    return PERIODICAL.getRepresentation();
  }

  @Override
  public Object getPrincipal() {
    return PERIODICAL;
  }
}
