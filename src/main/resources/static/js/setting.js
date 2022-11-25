$(function () {
    $("#uploadForm").submit(upload);
});

function upload() {
    //����ajax����֮ǰ����CSRF�������õ��������Ϣͷ��
    var token=$("meta[name='_csrf']").attr("content");
    var header=$("meta[name='_csrf_header']").attr("content");
    //������Ϣͷ��key��value
    $(document).ajaxSend(function (e,xhr,options){
        xhr.setRequestHeader(header,token);
    });

    $.ajax({
        url: "https://upload-z1.qiniup.com",
        method: "post",
        //������������תΪ�ַ���
        processData: false,
        //����jquery�����ϴ������ͣ���������Զ�����
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function (data) {
            if (data && data.code == 0) {
                //����ajax����֮ǰ����CSRF�������õ��������Ϣͷ��
                var token=$("meta[name='_csrf']").attr("content");
                var header=$("meta[name='_csrf_header']").attr("content");
                //������Ϣͷ��key��value
                $(document).ajaxSend(function (e,xhr,options){
                    xhr.setRequestHeader(header,token);
                });

                //����ͷ�����·��
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
                alert("�ϴ�ʧ�ܣ�");
            }
        }
    });
    return false;
}