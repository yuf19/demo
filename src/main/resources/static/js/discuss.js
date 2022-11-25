$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

function like(btn, entityType, entityId, entityUserId, postId) {
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //设置消息头的key：value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
            } else {
                alert(data.msg);
            }
        }
    );
}

function setTop() {
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //设置消息头的key：value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").text($("#topBtn").text()=="置顶"?"取消置顶":"置顶");
            } else {
                alert(data.msg);
            }
        }
    );
}

function setWonderful() {
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //设置消息头的key：value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").text($("#wonderfulBtn").text()=="加精"?"取消加精":"加精");
            } else {
                alert(data.msg);
            }
        }
    );
}

function setDelete() {
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //设置消息头的key：value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#deleteBtn").attr("disabled", "disabled");
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}