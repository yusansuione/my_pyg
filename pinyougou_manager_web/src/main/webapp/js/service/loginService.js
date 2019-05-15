app.service("loginService", function ($http) {
    this.LoginName = function () {
        return $http.get("../login/getName.do")
    }
})