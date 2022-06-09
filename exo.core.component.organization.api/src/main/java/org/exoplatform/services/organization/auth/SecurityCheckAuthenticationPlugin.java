package org.exoplatform.services.organization.auth;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.organization.User;

public abstract class SecurityCheckAuthenticationPlugin extends BaseComponentPlugin {

  /**
   * check user authentication depends on the specified logic in the plugin
   *
   * @param user Target user to be authenticated
   * @throws Exception
   */
  public abstract void doCheck(User user) throws Exception;

  /**
   * Invoked when the check on the authentication is failed
   * 
   * @param userName Target username
   */
  public abstract void onCheckFail(String userName);

  /**
   * Invoked when the check on the authentication is succeeded
   * 
   * @param userName Target username
   */
  public abstract void onCheckSuccess(String userName);
}
