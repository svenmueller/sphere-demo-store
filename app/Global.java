
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Results;
import play.mvc.SimpleResult;


import com.google.common.base.Optional;

public class Global extends GlobalSettings {



    @Override
    public Action onRequest(final Request request, final Method method) {
        return new Action.Simple() {
          
            @Override
            public F.Promise<SimpleResult> call(final Http.Context ctx) throws Throwable {
                if (invalidAuthorization(ctx)) {
                    return requestAuthorization(ctx);
                }
                return delegate.call(ctx);
            }

            private boolean invalidAuthorization(final Http.Context ctx) {
                String auth = ctx.request().getHeader("Authorization");
                if (auth == null) {
                    return true;
                }
                // Case wrong arguments provided
                String[] comps = auth.split(" ");
                if (comps.length != 2) {
                    return true;
                }
                // Case wrong format provided
                if (!"Basic".equals(comps[0])) {
                    return true;
                }
                // Case matching credentials
                String credentials = StringUtils.newStringUtf8(Base64.decodeBase64(comps[1]));
                // hardcode check just for deoming
                return !"hello:sphere".equals(credentials);
            }
            
            private F.Promise<SimpleResult> requestAuthorization(final Http.Context ctx) {
                ctx.response().setHeader("WWW-Authenticate", "Basic realm=\"Secured\"");
                return F.Promise.<SimpleResult> pure(unauthorized());
            }

        };
    }

}
