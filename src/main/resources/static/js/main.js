$(document).ready(function() {

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    $('#appConfig')
        .bootstrapValidator({
            message: 'This value is not valid',
            live: 'submitted',
            feedbackIcons: {
                valid: 'glyphicon glyphicon-ok',
                invalid: 'glyphicon glyphicon-remove',
                validating: 'glyphicon glyphicon-refresh'
            },
            fields: {
                platform: {
                    validators: {
                        notEmpty: {
                            message: '平台不能为空'
                        }
                    }
                },
                appId: {
                    validators: {
                        notEmpty: {
                            message: 'appId不能为空'
                        }
                    }
                },
                version: {
                    validators: {
                        notEmpty: {
                            message: '版本不能为空'
                        }
                    }
                },
                versionName: {
                    validators: {
                        notEmpty: {
                            message: '版本名称不能为空'
                        }
                    }
                },
                updateType: {
                    validators: {
                        notEmpty: {
                            message: '更新方式不能为空'
                        }
                    }
                },
                updateTips: {
                    validators: {
                        notEmpty: {
                            message: '更新提示语不能为空'
                        }
                    }
                },
            }
        })
        .on('success.form.bv', function(e) {
            e.preventDefault();
            var $form = $(e.target);
            var bv = $form.data('bootstrapValidator');
            jQuery.ajax({
                url: "/file/upload",
                data: new FormData(this),
                cache: false,
                contentType: false,
                processData: false,
                method: 'POST',
                success: function(data) {
                    if (data.code === 102001003) {
                        alert("版本已存在");
                    } else {
                        alert("上传成功");
                        $("#appConfig")[0].reset();
                    }
                }
            });
        });
    $('input[name="platform"]').on('ifChecked', function(event){
        var platform = $('input[name="platform"]:checked').val();
        if (platform == 1) {
            $("#appId").val("cn.android.mingzhi.motv");
            $("#inputFileDiv").show();
        } else {
            $("#appId").val("1271275802");
            $("#inputFileDiv").hide();
        }
    })
});