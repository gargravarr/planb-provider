package org.zalando.planb.provider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestRealm implements Realm {
    @Override
    public Map<String, Object> authenticate(final String user, final String password, final String[] scopes)
            throws AuthenticationFailedException {
        if (!password.equals("test")) {
            throw new Realm.AuthenticationFailedException();
        }

        return new HashMap<String, Object>() {{
            put("uid", user);
        }};
    }
}
