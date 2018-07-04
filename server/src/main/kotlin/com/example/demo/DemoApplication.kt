package com.example.demo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE
import org.springframework.http.HttpHeaders.IF_NONE_MATCH
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Instant
import java.util.concurrent.TimeUnit.MINUTES

@ServletComponentScan
@SpringBootApplication
open class DemoApplication

@RestController
@Suppress("unused")
class HelloController {
    private val myEtag = "an_etag_header"
    private val resourceLastModified = Instant.parse("2018-07-04T13:15:30.00Z").toEpochMilli()

    @RequestMapping("/")
    fun index() = "NOTHING"

    @RequestMapping("/etag")
    fun etag(swr: ServletWebRequest): ResponseEntity<String> {
        LOG.info("GET /etag with {} of {}", IF_NONE_MATCH, swr.getHeader(IF_NONE_MATCH))
        return if (!swr.checkNotModified(myEtag)) {
            LOG.info("No match: returning \"fresh\" 200 data")
            ResponseEntity.ok()
                    .eTag(myEtag).cacheControl(CacheControl.noCache())
                    .body("Greetings with ETag $myEtag at ${Instant.now()}")
        } else {
            LOG.info("Match: returning 304")
            ResponseEntity.status(NOT_MODIFIED).build()
        }
    }

    @RequestMapping("/last-modified")
    fun lastModified(swr: ServletWebRequest): ResponseEntity<String> {
        LOG.info("GET /last-modified with {} of {}", IF_MODIFIED_SINCE, swr.getHeader(IF_MODIFIED_SINCE))
        return if (!swr.checkNotModified(resourceLastModified)) {
            LOG.info("No match: returning \"fresh\" 200 data")
            ResponseEntity.ok()
                    .lastModified(resourceLastModified).cacheControl(CacheControl.noCache())
                    .body("Greetings using Last-Modified of " +
                            "${Instant.ofEpochMilli(resourceLastModified)} at ${Instant.now()}")
        } else {
            LOG.info("Match: returning 304")
            ResponseEntity.status(NOT_MODIFIED).build()
        }
    }

    @RequestMapping("/etag-last-modified")
    fun etagLastModified(swr: ServletWebRequest): ResponseEntity<String> {
        LOG.info("GET /etag-last-modified with {} of {} and {} of {}", IF_MODIFIED_SINCE, swr.getHeader(IF_MODIFIED_SINCE),
                IF_NONE_MATCH, swr.getHeader(IF_NONE_MATCH))
        return if (!swr.checkNotModified(myEtag, resourceLastModified)) {
            LOG.info("No match: returning \"fresh\" 200 data")
            ResponseEntity.ok()
                    .lastModified(resourceLastModified).eTag(myEtag).cacheControl(CacheControl.noCache())
                    .body("Greetings using ETag $myEtag and Last-Modified of " +
                            "${Instant.ofEpochMilli(resourceLastModified)} at ${Instant.now()}")
        } else {
            LOG.info("Match: returning 304")
            ResponseEntity.status(NOT_MODIFIED).build()
        }
    }

    @RequestMapping("/etag-1m")
    fun etagOneMinute(swr: ServletWebRequest): ResponseEntity<String> {
        LOG.info("GET /etag-1m with {} of {}", IF_NONE_MATCH, swr.getHeader(IF_NONE_MATCH))
        return if (!swr.checkNotModified(myEtag)) {
            LOG.info("No match: returning \"fresh\" 200 data")
            ResponseEntity.ok()
                    .eTag(myEtag).cacheControl(CacheControl.maxAge(1, MINUTES))
                    .body("Greetings using ETag $myEtag and max-age of 1m at ${Instant.now()}")
        } else {
            LOG.info("Match: returning 304")
            ResponseEntity.status(NOT_MODIFIED).build()
        }
    }

    @RequestMapping("/last-modified-1m")
    fun lastModifiedOneMinute(swr: ServletWebRequest): ResponseEntity<String> {
        LOG.info("GET /last-modified-1m with {} of {}", IF_MODIFIED_SINCE, swr.getHeader(IF_MODIFIED_SINCE))
        return if (!swr.checkNotModified(resourceLastModified)) {
            LOG.info("No match: returning \"fresh\" 200 data")
            ResponseEntity.ok()
                    .lastModified(resourceLastModified).cacheControl(CacheControl.maxAge(1, MINUTES))
                    .body("Greetings using Last-Modified of " +
                            "${Instant.ofEpochMilli(resourceLastModified)} and max-age 1m at ${Instant.now()}")
        } else {
            LOG.info("Match: returning 304")
            ResponseEntity.status(NOT_MODIFIED).build()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Configuration
open class MyConfiguration {
    @Bean
    open fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").exposedHeaders("ETag")
            }
        }
    }
}

val LOG: Logger = LoggerFactory.getLogger("Main")


//@WebFilter(urlPatterns = ["/*"])
//class HeaderLogger : Filter {
//
//    @Throws(ServletException::class)
//    override fun init(filterConfig: FilterConfig) {
//    }
//
//    @Throws(IOException::class, ServletException::class)
//    override fun doFilter(request: ServletRequest, response: ServletResponse,
//                 chain: FilterChain) {
//        val req = request as HttpServletRequest
//        val rep = response as HttpServletResponse
//        println("----- Request ---------")
//        Collections.list(req.getHeaderNames())
//                .forEach { n -> println(n + ": " + req.getHeader(n)) }
//
//        chain.doFilter(request, response)
//
//        println("----- response ---------")
//        rep.headerNames
//                .forEach { n -> println(n + ": " + rep.getHeader(n)) }
//
//        println("response status: " + rep.status)
//    }
//
//    override fun destroy() {}
//}
