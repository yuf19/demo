$(function () {
    $(".follow-btn").click(follow);
});

function follow() {
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //设置消息头的key：value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA
        $.post(
            CONTEXT_PATH + "/follow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注
        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}