app.service("loginService", function ($http) {
    this.loginUsername = function () {
        return $http.get("../login/getname.do")
    }
})