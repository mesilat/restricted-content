package com.mesilat;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import java.net.URI;
import java.util.Arrays;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

@Path("/icons")
@Scanned
public class IconsResource {
    private final UserManager userManager;
    private final SettingsManager settingsManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
    public Response get(@QueryParam("permissions") String permissions){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode images = mapper.createObjectNode();
        if (permissions != null){
            Arrays.asList(permissions.split(",")).stream().forEach((grantee)->{
                UserProfile userProfile = userManager.getUserProfile(grantee);
                if (userProfile == null){
                    images.put(grantee, getBaseUrl() + "/images/icons/avatar_group_48.png");
                } else {
                    URI pictureUri = userProfile.getProfilePictureUri();
                    images.put(grantee, pictureUri == null? getBaseUrl() + "/images/icons/profilepics/default.png": pictureUri.toString());
                }
            });
        }
        return Response.ok(images).build();
    }
    private String getBaseUrl(){
        return settingsManager.getGlobalSettings().getBaseUrl();
    }

    @Inject
    public IconsResource(
        final @ComponentImport UserManager userManager,
        final @ComponentImport SettingsManager settingsManager
    ){
        this.userManager = userManager;
        this.settingsManager = settingsManager;
    }
}