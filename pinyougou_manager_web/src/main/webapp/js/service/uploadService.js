app.service("uploadService", function ($http) {
    this.uploadService = function () {
        //fromData是h5表单对象，用于封装表单元素
        var formData = new FormData();
        //追加表单元素，第二个参数file为表单id取第一个
        formData.append("file", file.files[0])
        return $http({
            method: 'POST',
            url: '../upload.do',
            data: formData,
            //anjularjs对于post和get请求默认的Content-Type header 是application/json。
            //通过设置‘Content-Type’: undefined，
            // 这样浏览器会帮我们把Content-Type 设置为 multipart/form-data.
            headers: {'Content-Type': undefined},
            //通过设置 transformRequest: angular.identity ，
            // anjularjs transformRequest function 将序列化我们的formdata object.
            transformRequest: angular.identity
        })
    }
})