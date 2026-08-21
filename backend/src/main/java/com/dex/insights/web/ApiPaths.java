package com.dex.insights.web;

/**
 * Central definition of the API version and every endpoint path, mirrored by the UI's
 * {@code api.config.ts}. Bumping the version, or moving a resource, is a one-line change here
 * instead of a grep across controllers.
 *
 * <p>{@code @RequestMapping} requires compile-time constant expressions, so this is a plain
 * constants holder rather than a {@code @ConfigurationProperties} bean — {@code static final}
 * fields built from literals satisfy that requirement while still living in one place.
 */
public final class ApiPaths {

    public static final String VERSION = "v1";
    private static final String ROOT = "/" + VERSION;

    /** CORS-mapping wildcard covering every endpoint under this version. */
    public static final String VERSION_WILDCARD = ROOT + "/**";

    public static final String STORES = ROOT + "/stores";
    public static final String INSIGHTS = ROOT + "/insights";
    public static final String CHAT = ROOT + "/chat";

    private ApiPaths() {
    }
}
