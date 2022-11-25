$(function () {
    $("#uploadForm").submit(upload);
});

function upload() {
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //设置消息头的key：value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    $.ajax({
        url: "https://upload-z1.qiniup.com",
        method: "post",
        //不将表单的内容转为字符串
        processData: false,
        //不让jquery设置上传的类型，浏览器会自动设置
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function (data) {
            if (data && data.code == 0) {
                //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
                var token=$("meta[name='_csrf']").attr("content");
                var header=$("meta[name='_csrf_header']").attr("content");
                //设置消息头的key：value
                $(document).ajaxSend(function (e,xhr,options){
                    xhr.setRequestHeader(header,token);
                });

                //更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName": $("input[name='key']").val()},
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败！");
            }
        }
    });
    return false;
}