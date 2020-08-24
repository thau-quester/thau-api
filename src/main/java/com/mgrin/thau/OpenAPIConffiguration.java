package com.mgrin.thau;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(title = "Thau", description = "Thau - Ready-to-use authentication service for your application.", contact = @Contact(name = "Maintainer", url = "https://github.com/thau-quester/thau-api/issues/new", email = "mr6r1n@gmail.com"), license = @License(name = "MIT Licence", url = "https://github.com/thombergs/code-examples/blob/master/LICENSE")), servers = {
        @Server(url = "http://localhost:9000/api/v1"), @Server(url = "https://thau.quester-app.dev/api/v1") })
class OpenAPIConfiguration {
}