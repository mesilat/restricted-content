(function(AJS,$){
    function initUserGroupSelector($picker){
        var showNoResultsIfAllResultsDisabled = true;
        $picker.attr('data-autocomplete-bound', 'true');

        return $picker.auiSelect2({
            multiple: true,
            minimumInputLength: 2,
            formatInputTooShort: function () {
                return AJS.I18n.getText('oracle-htp-plugin.config.dad.grantees.prompt');
            },
            ajax: {
                transport: function(opts) {
                    // Workaround for Select2 bug: https://github.com/ivaynberg/select2/issues/381
                    // Select2 does not display "no results found" if the only results are already selected.
                    var success = opts.success;
                    delete opts.success;
                    return $.ajax.apply($.ajax, arguments).done(success).done(showNoResultsIfAllResultsDisabled);
                },
                data: function (searchTerm) {
                    return {
                        'max-results': 6,
                        query: searchTerm
                    };
                },
                dataType: 'json',
                url: Confluence.getContextPath() + '/rest/prototype/1/search/user-or-group.json',
                results: function (data) {
                    var results = [];
                    $.each(data.result, function () {
                        if (this.type === 'user') {
                            results.push({
                                id: this.username,
                                text: this.title,
                                imgSrc: this.thumbnailLink.href,
                                entity: this
                            });
                        } else {
                            results.push({
                                id: this.name,
                                text: this.name,
                                imgSrc: AJS.contextPath() + '/images/icons/avatar_group_48.png',
                                entity: this
                            });
                        }
                    });
                    return {
                        results: results
                    };
                },
                quietMillis: 300
            },
            formatResult: function (result) {
                return Confluence.UI.Components.UserGroupSelect2.avatarWithName({
                        size: 'small',
                        displayName: result.text,
                        userId: result.id,
                        avatarImageUrl: result.imgSrc
                    });
            },
            // common.Widget.avatarWithName handles escaping so this doesn't have to
            escapeMarkup: function (markup) {
                return markup;
            },
            formatSelection: function(result) {
                return Confluence.UI.Components.UserGroupSelect2.avatarWithName({
                        size: 'xsmall',
                        displayName: result.text,
                        userId: result.id,
                        avatarImageUrl: result.imgSrc
                    });
            },
            initSelection: function($elt, callback) {
                var data = [];
                var images = $elt.prop('images');
                $elt.val().split(',').forEach(function(userOrGroup){
                    var self = {
                        id: userOrGroup,
                        text: userOrGroup,
                        entity: self
                    };
                    if (typeof images !== 'undefined'){
                        self.imgSrc = images[self.id];
                    }                    
                    data.push(self);
                });
                callback(data);
            },
            dropdownCssClass: 'users-dropdown',
            containerCssClass: 'users-autocomplete',
            hasAvatar: true
        });
    }

    AJS.MacroBrowser.setMacroJsOverride('restricted-content', {
        fields: {
            'string': function(param,options){
                if (param.name === 'permissions'){
                    var $paramDiv = $(Confluence.Templates.MacroBrowser.macroParameter());
                    var $input = $('input', $paramDiv);
                    $input.addClass('autocomplete-multiusergroup');
                    initUserGroupSelector($input);
                    options = options || {};
                    options.setValue = options.setValue || function (value){
                        $.ajax({
                            url: AJS.contextPath() + '/rest/restricted-content/1.0/icons',
                            data: {
                                permissions: value
                            },
                            dataType: 'json'
                        }).done(function(data){
                            $input.prop('images',data);
                        }).fail(function(jqxhr){
                            console.error('restricted-content', jqxhr.responseText);
                        }).always(function(){
                            $input.val(value).trigger('change');
                        });
                    };
                    return AJS.MacroBrowser.Field($paramDiv, $input, options);

                } else {
                    var $paramDiv = $(Confluence.Templates.MacroBrowser.macroParameter());
                    var $input = $('input', $paramDiv);
                    if (param.required) {
                        $input.keyup(AJS.MacroBrowser.processRequiredParameters);
                    }
                    return AJS.MacroBrowser.Field($paramDiv, $input, options);
                }
            }
        }
    });
})(AJS,AJS.$||$);