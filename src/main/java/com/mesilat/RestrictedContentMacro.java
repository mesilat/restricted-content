package com.mesilat;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@Scanned
public class RestrictedContentMacro implements Macro {
    private final TemplateRenderer renderer;
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private final I18nResolver resolver;

    @Override
    public Macro.BodyType getBodyType(){
        return Macro.BodyType.RICH_TEXT;
    }
    @Override
    public Macro.OutputType getOutputType(){
        return Macro.OutputType.BLOCK;
    }
    @Override
    public String execute(Map params, String body, ConversionContext conversionContext) throws MacroExecutionException {
        ConfluenceUser user = AuthenticatedUserThreadLocal.get();
        if (user == null){
            return renderFromSoy("Mesilat.RestrictedContent.Templates.noPermission.soy", new HashMap<>());
        }
        if (permissionManager.hasPermission(user, Permission.EDIT, conversionContext.getEntity())){
            return body;
        }
        if (params.containsKey("permissions")){
            String[] perm = params.get("permissions").toString().split(",");
            for (int i = 0; i < perm.length; i++){
                String grantee = perm[i].trim();
                if (user.getName().equals(grantee) || userManager.isUserInGroup(user.getKey(), grantee)){
                    return body;
                }
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("message", params.containsKey("message")? params.get("message"): resolver.getText("com.mesilat.restricted-content.restricted-content.notpermitted.default"));
        return renderFromSoy("Mesilat.RestrictedContent.Templates.noPermission.soy", map);
    }

    @Inject
    public RestrictedContentMacro(
        final @ComponentImport TemplateRenderer renderer,
        final @ComponentImport PermissionManager permissionManager,
        final @ComponentImport UserManager userManager,
        final @ComponentImport I18nResolver resolver
    ){
        this.renderer = renderer;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.resolver = resolver;
    }

    public String renderFromSoy(String soyTemplate, Map soyContext) {
        StringBuilder output = new StringBuilder();
        renderer.renderTo(output, "com.mesilat.restricted-content:resources", soyTemplate, soyContext);
        return output.toString();
    }
}