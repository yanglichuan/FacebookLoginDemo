/*
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.login;

import static org.junit.Assert.*;

import com.facebook.AccessToken;
import com.facebook.AuthenticationToken;
import com.facebook.AuthenticationTokenClaims;
import com.facebook.FacebookTestCase;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class LoginResultTest extends FacebookTestCase {

  private final Set<String> EMAIL_SET =
      new HashSet<String>() {
        {
          add("email");
        }
      };
  private final Set<String> LIKES_EMAIL_SET =
      new HashSet<String>() {
        {
          add("user_likes");
          add("email");
        }
      };
  private final Set<String> PROFILE_EMAIL_SET =
      new HashSet<String>() {
        {
          add("user_profile");
          add("email");
        }
      };

  @Test
  public void testInitialLogin() {
    LoginClient.Request request = createRequest(EMAIL_SET, false);
    AccessToken accessToken =
        createAccessToken(PROFILE_EMAIL_SET, new HashSet<String>(), new HashSet<String>());
    AuthenticationToken authenticationToken = createAuthenticationToken();
    LoginResult result = LoginManager.computeLoginResult(request, accessToken, authenticationToken);
    assertEquals(accessToken, result.getAccessToken());
    assertEquals(authenticationToken, result.getAuthenticationToken());
    assertEquals(PROFILE_EMAIL_SET, result.getRecentlyGrantedPermissions());
    assertEquals(0, result.getRecentlyDeniedPermissions().size());
  }

  @Test
  public void testReAuth() {
    LoginClient.Request request = createRequest(EMAIL_SET, true);
    AccessToken accessToken =
        createAccessToken(PROFILE_EMAIL_SET, new HashSet<String>(), new HashSet<String>());
    AuthenticationToken authenticationToken = createAuthenticationToken();
    LoginResult result = LoginManager.computeLoginResult(request, accessToken, authenticationToken);
    assertEquals(accessToken, result.getAccessToken());
    assertEquals(authenticationToken, result.getAuthenticationToken());
    assertEquals(EMAIL_SET, result.getRecentlyGrantedPermissions());
    assertEquals(0, result.getRecentlyDeniedPermissions().size());
  }

  @Test
  public void testDeniedPermissions() {
    LoginClient.Request request = createRequest(LIKES_EMAIL_SET, true);
    AccessToken accessToken =
        createAccessToken(EMAIL_SET, new HashSet<String>(), new HashSet<String>());
    AuthenticationToken authenticationToken = createAuthenticationToken();
    LoginResult result = LoginManager.computeLoginResult(request, accessToken, authenticationToken);
    assertEquals(accessToken, result.getAccessToken());
    assertEquals(authenticationToken, result.getAuthenticationToken());
    assertEquals(EMAIL_SET, result.getRecentlyGrantedPermissions());
    assertEquals(
        new HashSet<String>() {
          {
            add("user_likes");
          }
        },
        result.getRecentlyDeniedPermissions());
  }

  private AccessToken createAccessToken(
      Set<String> permissions, Set<String> declinedPermissions, Set<String> expiredPermissions) {
    return new AccessToken(
        "token",
        "123",
        "234",
        permissions,
        declinedPermissions,
        expiredPermissions,
        null,
        null,
        null,
        null);
  }

  private AuthenticationToken createAuthenticationToken() {
    // TODO T93958663: Need to update this test with real valid id_token string after Validation
    // added
    String encodedHeader = "eyJhbGciOiJTSEEyNTYiLCJ0eXAiOiJ0b2tlbl90eXBlIiwia2lkIjoiYWJjIn0=";
    Date currentDate = new Date();
    AuthenticationTokenClaims claims =
        new AuthenticationTokenClaims(
            "jti",
            "iss",
            "aud",
            "nonce",
            new Date(currentDate.getTime() + 60), // add 60 minutes for the exp time
            currentDate,
            "sub");
    StringBuilder sb = new StringBuilder();
    sb.append(encodedHeader).append(".");
    sb.append(claims.toEnCodedString()).append(".");
    sb.append("Signature");
    return new AuthenticationToken(sb.toString());
  }

  private LoginClient.Request createRequest(Set<String> permissions, boolean isRerequest) {
    LoginClient.Request request =
        new LoginClient.Request(
            LoginBehavior.NATIVE_WITH_FALLBACK,
            permissions,
            DefaultAudience.EVERYONE,
            "rerequest",
            "123",
            "authid");
    request.setRerequest(isRerequest);
    return request;
  }
}
