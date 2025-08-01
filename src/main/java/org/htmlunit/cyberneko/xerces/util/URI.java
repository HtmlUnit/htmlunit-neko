/*
 * Copyright (c) 2017-2024 Ronald Brill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.cyberneko.xerces.util;

import java.io.IOException;
import java.util.Locale;

/**
 * A class to represent a Uniform Resource Identifier (URI). This class is
 * designed to handle the parsing of URIs and provide access to the various
 * components (scheme, host, port, userinfo, path, query string and fragment)
 * that may constitute a URI.
 * <p>
 * Parsing of a URI specification is done according to the URI syntax described
 * in <a href="http://www.ietf.org/rfc/rfc2396.txt?number=2396">RFC 2396</a>,
 * and amended by <a href="http://www.ietf.org/rfc/rfc2732.txt?number=2732">RFC
 * 2732</a>.
 * <p>
 * Every absolute URI consists of a scheme, followed by a colon (':'), followed
 * by a scheme-specific part. For URIs that follow the "generic URI" syntax, the
 * scheme-specific part begins with two slashes ("//") and may be followed by an
 * authority segment (comprised of user information, host, and port), path
 * segment, query segment and fragment. Note that RFC 2396 no longer specifies
 * the use of the parameters segment and excludes the "user:password" syntax as
 * part of the authority segment. If "user:password" appears in a URI, the
 * entire user/password string is stored as userinfo.
 * <p>
 * For URIs that do not follow the "generic URI" syntax (e.g. mailto), the
 * entire scheme-specific part is treated as the "path" portion of the URI.
 * <p>
 * Note that, unlike the java.net.URL class, this class does not provide any
 * built-in network access functionality nor does it provide any scheme-specific
 * functionality (for example, it does not know a default port for a specific
 * scheme). Rather, it only knows the grammar and basic set of operations that
 * can be applied to a URI.
 *
 */
public class URI {

    /**
     * MalformedURIExceptions are thrown in the process of building a URI or setting
     * fields on a URI when an operation would result in an invalid URI
     * specification.
     *
     */
    public static class MalformedURIException extends IOException {

        /**
         * Constructs a <code>MalformedURIException</code> with the specified detail
         * message.
         *
         * @param msg the detail message.
         */
        public MalformedURIException(final String msg) {
            super(msg);
        }
    }

    private static final byte[] FG_LOOKUP_TABLE = new byte[128];

    /** reserved characters ;/?:@&=+$,[] */
    // RFC 2732 added '[' and ']' as reserved characters
    private static final int RESERVED_CHARACTERS = 0x01;

    /**
     * URI punctuation mark characters: -_.!~*'() - these, combined with
     * alphanumerics, constitute the "unreserved" characters
     */
    private static final int MARK_CHARACTERS = 0x02;

    /** scheme can be composed of alphanumerics and these characters: +-. */
    private static final int SCHEME_CHARACTERS = 0x04;

    /**
     * userinfo can be composed of unreserved, escaped and these characters: ;:&=+$,
     */
    private static final int USERINFO_CHARACTERS = 0x08;

    /** ASCII letter characters */
    private static final int ASCII_ALPHA_CHARACTERS = 0x10;

    /** ASCII digit characters */
    private static final int ASCII_DIGIT_CHARACTERS = 0x20;

    /** ASCII hex characters */
    private static final int ASCII_HEX_CHARACTERS = 0x40;

    /** Path characters */
    private static final int PATH_CHARACTERS = 0x80;

    /** Mask for alpha-numeric characters */
    private static final int MASK_ALPHA_NUMERIC = ASCII_ALPHA_CHARACTERS | ASCII_DIGIT_CHARACTERS;

    /** Mask for unreserved characters */
    private static final int MASK_UNRESERVED_MASK = MASK_ALPHA_NUMERIC | MARK_CHARACTERS;

    /** Mask for URI allowable characters except for % */
    private static final int MASK_URI_CHARACTER = MASK_UNRESERVED_MASK | RESERVED_CHARACTERS;

    /** Mask for scheme characters */
    private static final int MASK_SCHEME_CHARACTER = MASK_ALPHA_NUMERIC | SCHEME_CHARACTERS;

    /** Mask for userinfo characters */
    private static final int MASK_USERINFO_CHARACTER = MASK_UNRESERVED_MASK | USERINFO_CHARACTERS;

    /** Mask for path characters */
    private static final int MASK_PATH_CHARACTER = MASK_UNRESERVED_MASK | PATH_CHARACTERS;

    static {
        // Add ASCII Digits and ASCII Hex Numbers
        for (int i = '0'; i <= '9'; ++i) {
            FG_LOOKUP_TABLE[i] |= ASCII_DIGIT_CHARACTERS | ASCII_HEX_CHARACTERS;
        }

        // Add ASCII Letters and ASCII Hex Numbers
        for (int i = 'A'; i <= 'F'; ++i) {
            FG_LOOKUP_TABLE[i] |= ASCII_ALPHA_CHARACTERS | ASCII_HEX_CHARACTERS;
            FG_LOOKUP_TABLE[i + 0x00000020] |= ASCII_ALPHA_CHARACTERS | ASCII_HEX_CHARACTERS;
        }

        // Add ASCII Letters
        for (int i = 'G'; i <= 'Z'; ++i) {
            FG_LOOKUP_TABLE[i] |= ASCII_ALPHA_CHARACTERS;
            FG_LOOKUP_TABLE[i + 0x00000020] |= ASCII_ALPHA_CHARACTERS;
        }

        // Add Reserved Characters
        FG_LOOKUP_TABLE[';'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['/'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['?'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE[':'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['@'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['&'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['='] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['+'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['$'] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE[','] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE['['] |= RESERVED_CHARACTERS;
        FG_LOOKUP_TABLE[']'] |= RESERVED_CHARACTERS;

        // Add Mark Characters
        FG_LOOKUP_TABLE['-'] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['_'] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['.'] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['!'] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['~'] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['*'] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['\''] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE['('] |= MARK_CHARACTERS;
        FG_LOOKUP_TABLE[')'] |= MARK_CHARACTERS;

        // Add Scheme Characters
        FG_LOOKUP_TABLE['+'] |= SCHEME_CHARACTERS;
        FG_LOOKUP_TABLE['-'] |= SCHEME_CHARACTERS;
        FG_LOOKUP_TABLE['.'] |= SCHEME_CHARACTERS;

        // Add Userinfo Characters
        FG_LOOKUP_TABLE[';'] |= USERINFO_CHARACTERS;
        FG_LOOKUP_TABLE[':'] |= USERINFO_CHARACTERS;
        FG_LOOKUP_TABLE['&'] |= USERINFO_CHARACTERS;
        FG_LOOKUP_TABLE['='] |= USERINFO_CHARACTERS;
        FG_LOOKUP_TABLE['+'] |= USERINFO_CHARACTERS;
        FG_LOOKUP_TABLE['$'] |= USERINFO_CHARACTERS;
        FG_LOOKUP_TABLE[','] |= USERINFO_CHARACTERS;

        // Add Path Characters
        FG_LOOKUP_TABLE[';'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE['/'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE[':'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE['@'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE['&'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE['='] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE['+'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE['$'] |= PATH_CHARACTERS;
        FG_LOOKUP_TABLE[','] |= PATH_CHARACTERS;
    }

    /** Stores the scheme (usually the protocol) for this URI. */
    private String scheme_;

    /** If specified, stores the userinfo for this URI; otherwise null */
    private String userinfo_;

    /** If specified, stores the host for this URI; otherwise null */
    private String host_;

    /** If specified, stores the port for this URI; otherwise -1 */
    private int port_ = -1;

    /**
     * If specified, stores the registry based authority for this URI; otherwise -1
     */
    private String regAuthority_;

    /** If specified, stores the path for this URI; otherwise null */
    private String path_;

    /**
     * If specified, stores the query string for this URI; otherwise null.
     */
    private String queryString_;

    /** If specified, stores the fragment for this URI; otherwise null */
    private String fragment_;

    /**
     * Construct a new URI from a URI specification string. If the specification
     * follows the "generic URI" syntax, (two slashes following the first colon),
     * the specification will be parsed accordingly - setting the scheme, userinfo,
     * host,port, path, query string and fragment fields as necessary. If the
     * specification does not follow the "generic URI" syntax, the specification is
     * parsed into a scheme and scheme-specific part (stored as the path) only.
     *
     * @param uriSpec the URI specification string (cannot be null or empty)
     *
     * @exception MalformedURIException if p_uriSpec violates any syntax rules
     */
    public URI(final String uriSpec) throws MalformedURIException {
        this(null, uriSpec);
    }

    /**
     * Construct a new URI from a URI specification string. If the specification
     * follows the "generic URI" syntax, (two slashes following the first colon),
     * the specification will be parsed accordingly - setting the scheme, userinfo,
     * host,port, path, query string and fragment fields as necessary. If the
     * specification does not follow the "generic URI" syntax, the specification is
     * parsed into a scheme and scheme-specific part (stored as the path) only.
     * Construct a relative URI if boolean is assigned to "true" and p_uriSpec is
     * not valid absolute URI, instead of throwing an exception.
     *
     * @param uriSpec           the URI specification string (cannot be null or
     *                            empty)
     * @param allowNonAbsoluteURI true to permit non-absolute URIs, false otherwise.
     *
     * @exception MalformedURIException if p_uriSpec violates any syntax rules
     */
    public URI(final String uriSpec, final boolean allowNonAbsoluteURI) throws MalformedURIException {
        this(null, uriSpec, allowNonAbsoluteURI);
    }

    /**
     * Construct a new URI from a base URI and a URI specification string. The URI
     * specification string may be a relative URI.
     *
     * @param base    the base URI (cannot be null if p_uriSpec is null or empty)
     * @param uriSpec the URI specification string (cannot be null or empty if
     *                  p_base is null)
     *
     * @exception MalformedURIException if p_uriSpec violates any syntax rules
     */
    public URI(final URI base, final String uriSpec) throws MalformedURIException {
        initialize(base, uriSpec);
    }

    /**
     * Construct a new URI from a base URI and a URI specification string. The URI
     * specification string may be a relative URI. Construct a relative URI if
     * boolean is assigned to "true" and p_uriSpec is not valid absolute URI and
     * p_base is null instead of throwing an exception.
     *
     * @param base              the base URI (cannot be null if p_uriSpec is null
     *                            or empty)
     * @param uriSpec           the URI specification string (cannot be null or
     *                            empty if p_base is null)
     * @param allowNonAbsoluteURI true to permit non-absolute URIs, false otherwise.
     *
     * @exception MalformedURIException if p_uriSpec violates any syntax rules
     */
    public URI(final URI base, final String uriSpec, final boolean allowNonAbsoluteURI) throws MalformedURIException {
        initialize(base, uriSpec, allowNonAbsoluteURI);
    }

    /**
     * Construct a new URI that follows the generic URI syntax from its component
     * parts. Each component is validated for syntax and some basic semantic checks
     * are performed as well. See the individual setter methods for specifics.
     *
     * @param scheme      the URI scheme (cannot be null or empty)
     * @param host        the hostname, IPv4 address or IPv6 reference for the URI
     * @param path        the URI path - if the path contains '?' or '#', then the
     *                      query string and/or fragment will be set from the path;
     *                      however, if the query and fragment are specified both in
     *                      the path and as separate parameters, an exception is
     *                      thrown
     * @param queryString the URI query string (cannot be specified if path is
     *                      null)
     * @param fragment    the URI fragment (cannot be specified if path is null)
     *
     * @exception MalformedURIException if any of the parameters violates syntax
     *                                  rules or semantic rules
     */
    public URI(final String scheme, final String host, final String path,
                final String queryString, final String fragment)
            throws MalformedURIException {
        this(scheme, null, host, -1, path, queryString, fragment);
    }

    /**
     * Construct a new URI that follows the generic URI syntax from its component
     * parts. Each component is validated for syntax and some basic semantic checks
     * are performed as well. See the individual setter methods for specifics.
     *
     * @param scheme      the URI scheme (cannot be null or empty)
     * @param userinfo    the URI userinfo (cannot be specified if host is null)
     * @param host        the hostname, IPv4 address or IPv6 reference for the URI
     * @param port        the URI port (may be -1 for "unspecified"; cannot be
     *                      specified if host is null)
     * @param path        the URI path - if the path contains '?' or '#', then the
     *                      query string and/or fragment will be set from the path;
     *                      however, if the query and fragment are specified both in
     *                      the path and as separate parameters, an exception is
     *                      thrown
     * @param queryString the URI query string (cannot be specified if path is
     *                      null)
     * @param fragment    the URI fragment (cannot be specified if path is null)
     *
     * @exception MalformedURIException if any of the parameters violates syntax
     *                                  rules or semantic rules
     */
    public URI(final String scheme, final String userinfo, final String host, final int port,
                final String path, final String queryString,
            final String fragment) throws MalformedURIException {
        if (scheme == null || scheme.trim().isEmpty()) {
            throw new MalformedURIException("Scheme is required!");
        }

        if (host == null) {
            if (userinfo != null) {
                throw new MalformedURIException("Userinfo may not be specified if host is not specified!");
            }
            if (port != -1) {
                throw new MalformedURIException("Port may not be specified if host is not specified!");
            }
        }

        if (path != null) {
            if (path.indexOf('?') != -1 && queryString != null) {
                throw new MalformedURIException("Query string cannot be specified in path and query string!");
            }

            if (path.indexOf('#') != -1 && fragment != null) {
                throw new MalformedURIException("Fragment cannot be specified in both the path and fragment!");
            }
        }

        setScheme(scheme);
        setHost(host);
        setPort(port);
        setUserinfo(userinfo);
        setPath(path);
        setQueryString(queryString);
        setFragment(fragment);
    }

    /**
     * Initialize all fields of this URI from another URI.
     *
     * @param other the URI to copy (cannot be null)
     */
    private void initialize(final URI other) {
        scheme_ = other.getScheme();
        userinfo_ = other.getUserinfo();
        host_ = other.getHost();
        port_ = other.getPort();
        regAuthority_ = other.getRegBasedAuthority();
        path_ = other.getPath();
        queryString_ = other.getQueryString();
        fragment_ = other.getFragment();
    }

    /**
     * Initializes this URI from a base URI and a URI specification string. See RFC
     * 2396 Section 4 and Appendix B for specifications on parsing the URI and
     * Section 5 for specifications on resolving relative URIs and relative paths.
     *
     * @param base              the base URI (may be null if p_uriSpec is an
     *                            absolute URI)
     * @param uriSpec           the URI spec string which may be an absolute or
     *                            relative URI (can only be null/empty if p_base is
     *                            not null)
     * @param allowNonAbsoluteURI true to permit non-absolute URIs, in case of
     *                            relative URI, false otherwise.
     *
     * @exception MalformedURIException if p_base is null and p_uriSpec is not an
     *                                  absolute URI or if p_uriSpec violates syntax
     *                                  rules
     */
    private void initialize(final URI base, final String uriSpec,
                    final boolean allowNonAbsoluteURI) throws MalformedURIException {

        final int uriSpecLen = (uriSpec != null) ? uriSpec.length() : 0;

        if (base == null && uriSpecLen == 0) {
            if (allowNonAbsoluteURI) {
                path_ = "";
                return;
            }
            throw new MalformedURIException("Cannot initialize URI with empty parameters.");
        }

        // just make a copy of the base if spec is empty
        if (uriSpecLen == 0) {
            initialize(base);
            return;
        }

        int index = 0;

        // Check for scheme, which must be before '/', '?' or '#'.
        final int colonIdx = uriSpec.indexOf(':');
        if (colonIdx != -1) {
            final int searchFrom = colonIdx - 1;
            // search backwards starting from character before ':'.
            final int slashIdx = uriSpec.lastIndexOf('/', searchFrom);
            final int queryIdx = uriSpec.lastIndexOf('?', searchFrom);
            final int fragmentIdx = uriSpec.lastIndexOf('#', searchFrom);

            if (colonIdx == 0 || slashIdx != -1 || queryIdx != -1 || fragmentIdx != -1) {
                // A standalone base is a valid URI according to spec
                if (colonIdx == 0 || (base == null && fragmentIdx != 0 && !allowNonAbsoluteURI)) {
                    throw new MalformedURIException("No scheme found in URI.");
                }
            }
            else {
                initializeScheme(uriSpec);
                index = scheme_.length() + 1;

                // Neither 'scheme:' or 'scheme:#fragment' are valid URIs.
                if (colonIdx == uriSpecLen - 1 || uriSpec.charAt(colonIdx + 1) == '#') {
                    throw new MalformedURIException("Scheme specific part cannot be empty.");
                }
            }
        }
        else if (base == null && uriSpec.indexOf('#') != 0 && !allowNonAbsoluteURI) {
            throw new MalformedURIException("No scheme found in URI.");
        }

        // Two slashes means we may have authority, but definitely means we're either
        // matching net_path or abs_path. These two productions are ambiguous in that
        // every net_path (except those containing an IPv6Reference) is an abs_path.
        // RFC 2396 resolves this ambiguity by applying a greedy left most matching
        // rule.
        // Try matching net_path first, and if that fails we don't have authority so
        // then attempt to match abs_path.
        //
        // net_path = "//" authority [ abs_path ]
        // abs_path = "/" path_segments
        if (((index + 1) < uriSpecLen) && (uriSpec.charAt(index) == '/' && uriSpec.charAt(index + 1) == '/')) {
            index += 2;
            final int startPos = index;

            // Authority will be everything up to path, query or fragment
            while (index < uriSpecLen) {
                final char testChar = uriSpec.charAt(index);
                if (testChar == '/' || testChar == '?' || testChar == '#') {
                    break;
                }
                index++;
            }

            // Attempt to parse authority. If the section is an empty string
            // this is a valid server based authority, so set the host to this
            // value.
            if (index > startPos) {
                // If we didn't find authority we need to back up. Attempt to
                // match against abs_path next.
                if (!initializeAuthority(uriSpec.substring(startPos, index))) {
                    index = startPos - 2;
                }
            }
            else {
                host_ = "";
            }
        }

        initializePath(uriSpec, index);

        // Resolve relative URI to base URI - see RFC 2396 Section 5.2
        // In some cases, it might make more sense to throw an exception
        // (when scheme is specified is the string spec and the base URI
        // is also specified, for example), but we're just following the
        // RFC specifications
        if (base != null) {
            absolutize(base);
        }
    }

    /**
     * Initializes this URI from a base URI and a URI specification string. See RFC
     * 2396 Section 4 and Appendix B for specifications on parsing the URI and
     * Section 5 for specifications on resolving relative URIs and relative paths.
     *
     * @param base    the base URI (may be null if p_uriSpec is an absolute URI)
     * @param uriSpec the URI spec string which may be an absolute or relative URI
     *                  (can only be null/empty if p_base is not null)
     *
     * @exception MalformedURIException if p_base is null and p_uriSpec is not an
     *                                  absolute URI or if p_uriSpec violates syntax
     *                                  rules
     */
    private void initialize(final URI base, final String uriSpec) throws MalformedURIException {

        final int uriSpecLen = (uriSpec != null) ? uriSpec.length() : 0;

        if (base == null && uriSpecLen == 0) {
            throw new MalformedURIException("Cannot initialize URI with empty parameters.");
        }

        // just make a copy of the base if spec is empty
        if (uriSpecLen == 0) {
            initialize(base);
            return;
        }

        int index = 0;

        // Check for scheme, which must be before '/', '?' or '#'.
        final int colonIdx = uriSpec.indexOf(':');
        if (colonIdx != -1) {
            final int searchFrom = colonIdx - 1;
            // search backwards starting from character before ':'.
            final int slashIdx = uriSpec.lastIndexOf('/', searchFrom);
            final int queryIdx = uriSpec.lastIndexOf('?', searchFrom);
            final int fragmentIdx = uriSpec.lastIndexOf('#', searchFrom);

            if (colonIdx == 0 || slashIdx != -1 || queryIdx != -1 || fragmentIdx != -1) {
                // A standalone base is a valid URI according to spec
                if (colonIdx == 0 || (base == null && fragmentIdx != 0)) {
                    throw new MalformedURIException("No scheme found in URI.");
                }
            }
            else {
                initializeScheme(uriSpec);
                index = scheme_.length() + 1;

                // Neither 'scheme:' or 'scheme:#fragment' are valid URIs.
                if (colonIdx == uriSpecLen - 1 || uriSpec.charAt(colonIdx + 1) == '#') {
                    throw new MalformedURIException("Scheme specific part cannot be empty.");
                }
            }
        }
        else if (base == null && uriSpec.indexOf('#') != 0) {
            throw new MalformedURIException("No scheme found in URI.");
        }

        // Two slashes means we may have authority, but definitely means we're either
        // matching net_path or abs_path. These two productions are ambiguous in that
        // every net_path (except those containing an IPv6Reference) is an abs_path.
        // RFC 2396 resolves this ambiguity by applying a greedy left most matching
        // rule.
        // Try matching net_path first, and if that fails we don't have authority so
        // then attempt to match abs_path.
        //
        // net_path = "//" authority [ abs_path ]
        // abs_path = "/" path_segments
        if (((index + 1) < uriSpecLen) && (uriSpec.charAt(index) == '/' && uriSpec.charAt(index + 1) == '/')) {
            index += 2;
            final int startPos = index;

            // Authority will be everything up to path, query or fragment
            while (index < uriSpecLen) {
                final char testChar = uriSpec.charAt(index);
                if (testChar == '/' || testChar == '?' || testChar == '#') {
                    break;
                }
                index++;
            }

            // Attempt to parse authority. If the section is an empty string
            // this is a valid server based authority, so set the host to this
            // value.
            if (index > startPos) {
                // If we didn't find authority we need to back up. Attempt to
                // match against abs_path next.
                if (!initializeAuthority(uriSpec.substring(startPos, index))) {
                    index = startPos - 2;
                }
            }
            else {
                host_ = "";
            }
        }

        initializePath(uriSpec, index);

        // Resolve relative URI to base URI - see RFC 2396 Section 5.2
        // In some cases, it might make more sense to throw an exception
        // (when scheme is specified is the string spec and the base URI
        // is also specified, for example), but we're just following the
        // RFC specifications
        if (base != null) {
            absolutize(base);
        }
    }

    /**
     * Absolutize URI with given base URI.
     *
     * @param base base URI for absolutization
     */
    public void absolutize(final URI base) {

        // check to see if this is the current doc - RFC 2396 5.2 #2
        // note that this is slightly different from the RFC spec in that
        // we don't include the check for query string being null
        // - this handles cases where the urispec is just a query
        // string or a fragment (e.g. "?y" or "#s") -
        // see <http://www.ics.uci.edu/~fielding/url/test1.html> which
        // identified this as a bug in the RFC
        if (path_.isEmpty() && scheme_ == null && host_ == null && regAuthority_ == null) {
            scheme_ = base.getScheme();
            userinfo_ = base.getUserinfo();
            host_ = base.getHost();
            port_ = base.getPort();
            regAuthority_ = base.getRegBasedAuthority();
            path_ = base.getPath();

            if (queryString_ == null) {
                queryString_ = base.getQueryString();

                if (fragment_ == null) {
                    fragment_ = base.getFragment();
                }
            }
            return;
        }

        // check for scheme - RFC 2396 5.2 #3
        // if we found a scheme, it means absolute URI, so we're done
        if (scheme_ == null) {
            scheme_ = base.getScheme();
        }
        else {
            return;
        }

        // check for authority - RFC 2396 5.2 #4
        // if we found a host, then we've got a network path, so we're done
        if (host_ == null && regAuthority_ == null) {
            userinfo_ = base.getUserinfo();
            host_ = base.getHost();
            port_ = base.getPort();
            regAuthority_ = base.getRegBasedAuthority();
        }
        else {
            return;
        }

        // check for absolute path - RFC 2396 5.2 #5
        if (!path_.isEmpty() && path_.startsWith("/")) {
            return;
        }

        // if we get to this point, we need to resolve relative path
        // RFC 2396 5.2 #6
        String path = "";
        final String basePath = base.getPath();

        // 6a - get all but the last segment of the base URI path
        if (basePath != null && !basePath.isEmpty()) {
            final int lastSlash = basePath.lastIndexOf('/');
            if (lastSlash != -1) {
                path = basePath.substring(0, lastSlash + 1);
            }
        }
        else if (!path_.isEmpty()) {
            path = "/";
        }

        // 6b - append the relative URI path
        path = path.concat(path_);

        // 6c - remove all "./" where "." is a complete path segment
        int index;
        while ((index = path.indexOf("/./")) != -1) {
            path = path.substring(0, index + 1).concat(path.substring(index + 3));
        }

        // 6d - remove "." if path ends with "." as a complete path segment
        if (path.endsWith("/.")) {
            path = path.substring(0, path.length() - 1);
        }

        // 6e - remove all "<segment>/../" where "<segment>" is a complete
        // path segment not equal to ".."
        index = 1;
        int segIndex = -1;
        String tempString = null;

        while ((index = path.indexOf("/../", index)) > 0) {
            tempString = path.substring(0, path.indexOf("/../"));
            segIndex = tempString.lastIndexOf('/');
            if (segIndex != -1) {
                if (!"..".equals(tempString.substring(segIndex))) {
                    path = path.substring(0, segIndex + 1).concat(path.substring(index + 4));
                    index = segIndex;
                }
                else {
                    index += 4;
                }
            }
            else {
                index += 4;
            }
        }

        // 6f - remove ending "<segment>/.." where "<segment>" is a
        // complete path segment
        if (path.endsWith("/..")) {
            tempString = path.substring(0, path.length() - 3);
            segIndex = tempString.lastIndexOf('/');
            if (segIndex != -1) {
                path = path.substring(0, segIndex + 1);
            }
        }
        path_ = path;
    }

    /**
     * Initialize the scheme for this URI from a URI string spec.
     *
     * @param uriSpec the URI specification (cannot be null)
     *
     * @exception MalformedURIException if URI does not have a conformant scheme
     */
    private void initializeScheme(final String uriSpec) throws MalformedURIException {
        final int uriSpecLen = uriSpec.length();
        int index = 0;

        while (index < uriSpecLen) {
            final char testChar = uriSpec.charAt(index);
            if (testChar == ':' || testChar == '/' || testChar == '?' || testChar == '#') {
                break;
            }
            index++;
        }

        final String scheme = uriSpec.substring(0, index);
        if (scheme.isEmpty()) {
            throw new MalformedURIException("No scheme found in URI.");
        }
        setScheme(scheme);
    }

    /**
     * Initialize the authority (either server or registry based) for this URI from
     * a URI string spec.
     *
     * @param uriSpec the URI specification (cannot be null)
     *
     * @return true if the given string matched server or registry based authority
     */
    private boolean initializeAuthority(final String uriSpec) {

        int index = 0;
        int start = 0;
        final int end = uriSpec.length();

        String userinfo = null;

        // userinfo is everything up to @
        if (uriSpec.indexOf('@', start) != -1) {
            while (index < end) {
                final char testChar = uriSpec.charAt(index);
                if (testChar == '@') {
                    break;
                }
                index++;
            }
            userinfo = uriSpec.substring(start, index);
            index++;
        }

        // host is everything up to last ':', or up to
        // and including ']' if followed by ':'.
        start = index;
        boolean hasPort = false;
        if (index < end) {
            if (uriSpec.charAt(start) == '[') {
                final int bracketIndex = uriSpec.indexOf(']', start);
                index = (bracketIndex != -1) ? bracketIndex : end;
                if (index + 1 < end && uriSpec.charAt(index + 1) == ':') {
                    ++index;
                    hasPort = true;
                }
                else {
                    index = end;
                }
            }
            else {
                final int colonIndex = uriSpec.lastIndexOf(':', end);
                index = (colonIndex > start) ? colonIndex : end;
                hasPort = index != end;
            }
        }

        final String host = uriSpec.substring(start, index);
        int port = -1;
        if (!host.isEmpty()) {
            // port
            if (hasPort) {
                index++;
                start = index;
                while (index < end) {
                    index++;
                }
                final String portStr = uriSpec.substring(start, index);
                if (!portStr.isEmpty()) {
                    // REVISIT: Remove this code.
                    // REVISIT: Remove this code.
                    // Store port value as string instead of integer.
                    try {
                        port = Integer.parseInt(portStr);
                        if (port == -1) {
                            --port;
                        }
                    }
                    catch (final NumberFormatException ex) {
                        port = -2;
                    }
                }
            }
        }

        if (isValidServerBasedAuthority(host, port, userinfo)) {
            host_ = host;
            port_ = port;
            userinfo_ = userinfo;
            return true;
        }
        // Note: Registry based authority is being removed from a
        // new spec for URI which would obsolete RFC 2396. If the
        // spec is added to XML errata, processing of reg_name
        // needs to be removed. - mrglavas.
        else if (isValidRegistryBasedAuthority(uriSpec)) {
            regAuthority_ = uriSpec;
            return true;
        }
        return false;
    }

    /**
     * Determines whether the components host, port, and user info are valid as a
     * server authority.
     *
     * @param host     the host component of authority
     * @param port     the port number component of authority
     * @param userinfo the user info component of authority
     *
     * @return true if the given host, port, and userinfo compose a valid server
     *         authority
     */
    private boolean isValidServerBasedAuthority(final String host, final int port, final String userinfo) {

        // Check if the host is well formed.
        // Check that port is well formed if it exists.
        // REVISIT: There's no restriction on port value ranges, but
        // perform the same check as in setPort to be consistent. Pass
        // in a string to this method instead of an integer.
        if (!isWellFormedAddress(host) || port < -1 || port > 65535) {
            return false;
        }

        // Check that userinfo is well formed if it exists.
        if (userinfo != null) {
            // Userinfo can contain alphanumerics, mark characters, escaped
            // and ';',':','&','=','+','$',','
            int index = 0;
            final int end = userinfo.length();
            while (index < end) {
                final char testChar = userinfo.charAt(index);
                if (testChar == '%') {
                    if (index + 2 >= end || !isHex(userinfo.charAt(index + 1)) || !isHex(userinfo.charAt(index + 2))) {
                        return false;
                    }
                    index += 2;
                }
                else if (!isUserinfoCharacter(testChar)) {
                    return false;
                }
                ++index;
            }
        }
        return true;
    }

    /**
     * Determines whether the given string is a registry based authority.
     *
     * @param authority the authority component of a URI
     *
     * @return true if the given string is a registry based authority
     */
    private boolean isValidRegistryBasedAuthority(final String authority) {
        int index = 0;
        final int end = authority.length();
        char testChar;

        while (index < end) {
            testChar = authority.charAt(index);

            // check for valid escape sequence
            if (testChar == '%') {
                if (index + 2 >= end || !isHex(authority.charAt(index + 1)) || !isHex(authority.charAt(index + 2))) {
                    return false;
                }
                index += 2;
            }
            // can check against path characters because the set
            // is the same except for '/' which we've already excluded.
            else if (!isPathCharacter(testChar)) {
                return false;
            }
            ++index;
        }
        return true;
    }

    /**
     * Initialize the path for this URI from a URI string spec.
     *
     * @param uriSpec     the URI specification (cannot be null)
     * @param nStartIndex the index to begin scanning from
     *
     * @exception MalformedURIException if p_uriSpec violates syntax rules
     */
    private void initializePath(final String uriSpec, final int nStartIndex) throws MalformedURIException {
        if (uriSpec == null) {
            throw new MalformedURIException("Cannot initialize path from null string!");
        }

        int index = nStartIndex;
        int start = nStartIndex;
        final int end = uriSpec.length();
        char testChar = '\0';

        // path - everything up to query string or fragment
        if (start < end) {
            // RFC 2732 only allows '[' and ']' to appear in the opaque part.
            if (getScheme() == null || uriSpec.charAt(start) == '/') {

                // Scan path.
                // abs_path = "/" path_segments
                // rel_path = rel_segment [ abs_path ]
                while (index < end) {
                    testChar = uriSpec.charAt(index);

                    // check for valid escape sequence
                    if (testChar == '%') {
                        if (index + 2 >= end || !isHex(uriSpec.charAt(index + 1))
                                || !isHex(uriSpec.charAt(index + 2))) {
                            throw new MalformedURIException("Path contains invalid escape sequence!");
                        }
                        index += 2;
                    }
                    // Path segments cannot contain '[' or ']' since pchar
                    // production was not changed by RFC 2732.
                    else if (!isPathCharacter(testChar)) {
                        if (testChar == '?' || testChar == '#') {
                            break;
                        }
                        throw new MalformedURIException("Path contains invalid character: " + testChar);
                    }
                    ++index;
                }
            }
            else {

                // Scan opaque part.
                // opaque_part = uric_no_slash *uric
                while (index < end) {
                    testChar = uriSpec.charAt(index);

                    if (testChar == '?' || testChar == '#') {
                        break;
                    }

                    // check for valid escape sequence
                    if (testChar == '%') {
                        if (index + 2 >= end || !isHex(uriSpec.charAt(index + 1))
                                || !isHex(uriSpec.charAt(index + 2))) {
                            throw new MalformedURIException("Opaque part contains invalid escape sequence!");
                        }
                        index += 2;
                    }
                    // If the scheme specific part is opaque, it can contain '['
                    // and ']'. uric_no_slash wasn't modified by RFC 2732, which
                    // I've interpreted as an error in the spec, since the
                    // production should be equivalent to (uric - '/'), and uric
                    // contains '[' and ']'. - mrglavas
                    else if (!isURICharacter(testChar)) {
                        throw new MalformedURIException("Opaque part contains invalid character: " + testChar);
                    }
                    ++index;
                }
            }
        }
        path_ = uriSpec.substring(start, index);

        // query - starts with ? and up to fragment or end
        if (testChar == '?') {
            index++;
            start = index;
            while (index < end) {
                testChar = uriSpec.charAt(index);
                if (testChar == '#') {
                    break;
                }
                if (testChar == '%') {
                    if (index + 2 >= end || !isHex(uriSpec.charAt(index + 1))
                            || !isHex(uriSpec.charAt(index + 2))) {
                        throw new MalformedURIException("Query string contains invalid escape sequence!");
                    }
                    index += 2;
                }
                else if (!isURICharacter(testChar)) {
                    throw new MalformedURIException("Query string contains invalid character: " + testChar);
                }
                index++;
            }
            queryString_ = uriSpec.substring(start, index);
        }

        // fragment - starts with #
        if (testChar == '#') {
            index++;
            start = index;
            while (index < end) {
                testChar = uriSpec.charAt(index);

                if (testChar == '%') {
                    if (index + 2 >= end || !isHex(uriSpec.charAt(index + 1))
                            || !isHex(uriSpec.charAt(index + 2))) {
                        throw new MalformedURIException("Fragment contains invalid escape sequence!");
                    }
                    index += 2;
                }
                else if (!isURICharacter(testChar)) {
                    throw new MalformedURIException("Fragment contains invalid character: " + testChar);
                }
                index++;
            }
            fragment_ = uriSpec.substring(start, index);
        }
    }

    /**
     * Get the scheme for this URI.
     *
     * @return the scheme for this URI
     */
    public String getScheme() {
        return scheme_;
    }

    /**
     * Get the scheme-specific part for this URI (everything following the scheme
     * and the first colon). See RFC 2396 Section 5.2 for spec.
     *
     * @return the scheme-specific part for this URI
     */
    public String getSchemeSpecificPart() {
        final StringBuilder schemespec = new StringBuilder();

        if (host_ != null || regAuthority_ != null) {
            schemespec.append("//");

            // Server based authority.
            if (host_ != null) {

                if (userinfo_ != null) {
                    schemespec.append(userinfo_);
                    schemespec.append('@');
                }

                schemespec.append(host_);

                if (port_ != -1) {
                    schemespec.append(':');
                    schemespec.append(port_);
                }
            }
            // Registry based authority.
            else {
                schemespec.append(regAuthority_);
            }
        }

        if (path_ != null) {
            schemespec.append(path_);
        }

        if (queryString_ != null) {
            schemespec.append('?');
            schemespec.append(queryString_);
        }

        if (fragment_ != null) {
            schemespec.append('#');
            schemespec.append(fragment_);
        }

        return schemespec.toString();
    }

    /**
     * Get the userinfo for this URI.
     *
     * @return the userinfo for this URI (null if not specified).
     */
    public String getUserinfo() {
        return userinfo_;
    }

    /**
     * Get the host for this URI.
     *
     * @return the host for this URI (null if not specified).
     */
    public String getHost() {
        return host_;
    }

    /**
     * Get the port for this URI.
     *
     * @return the port for this URI (-1 if not specified).
     */
    public int getPort() {
        return port_;
    }

    /**
     * Get the registry based authority for this URI.
     *
     * @return the registry based authority (null if not specified).
     */
    public String getRegBasedAuthority() {
        return regAuthority_;
    }

    /**
     * Get the path for this URI. Note that the value returned is the path only and
     * does not include the query string or fragment.
     *
     * @return the path for this URI.
     */
    public String getPath() {
        return path_;
    }

    /**
     * Get the query string for this URI.
     *
     * @return the query string for this URI. Null is returned if there was no "?"
     *         in the URI spec, empty string if there was a "?" but no query string
     *         following it.
     */
    public String getQueryString() {
        return queryString_;
    }

    /**
     * Get the fragment for this URI.
     *
     * @return the fragment for this URI. Null is returned if there was no "#" in
     *         the URI spec, empty string if there was a "#" but no fragment
     *         following it.
     */
    public String getFragment() {
        return fragment_;
    }

    /**
     * Set the scheme for this URI. The scheme is converted to lowercase before it
     * is set.
     *
     * @param scheme the scheme for this URI (cannot be null)
     *
     * @exception MalformedURIException if p_scheme is not a conformant scheme name
     */
    public void setScheme(final String scheme) throws MalformedURIException {
        if (scheme == null) {
            throw new MalformedURIException("Cannot set scheme from null string!");
        }
        if (!isConformantSchemeName(scheme)) {
            throw new MalformedURIException("The scheme is not conformant.");
        }

        scheme_ = scheme.toLowerCase(Locale.ROOT);
    }

    /**
     * Set the userinfo for this URI. If a non-null value is passed in and the host
     * value is null, then an exception is thrown.
     *
     * @param userinfo the userinfo for this URI
     *
     * @exception MalformedURIException if p_userinfo contains invalid characters
     */
    public void setUserinfo(final String userinfo) throws MalformedURIException {
        if (userinfo == null) {
            userinfo_ = null;
            return;
        }

        if (host_ == null) {
            throw new MalformedURIException("Userinfo cannot be set when host is null!");
        }

        // userinfo can contain alphanumerics, mark characters, escaped
        // and ';',':','&','=','+','$',','
        int index = 0;
        final int end = userinfo.length();
        while (index < end) {
            final char testChar = userinfo.charAt(index);
            if (testChar == '%') {
                if (index + 2 >= end || !isHex(userinfo.charAt(index + 1)) || !isHex(userinfo.charAt(index + 2))) {
                    throw new MalformedURIException("Userinfo contains invalid escape sequence!");
                }
            }
            else if (!isUserinfoCharacter(testChar)) {
                throw new MalformedURIException("Userinfo contains invalid character:" + testChar);
            }
            index++;
        }

        userinfo_ = userinfo;
    }

    /**
     * <p>
     * Set the host for this URI. If null is passed in, the userinfo field is also
     * set to null and the port is set to -1.
     * </p>
     *
     * <p>
     * Note: This method overwrites registry based authority if it previously
     * existed in this URI.
     * </p>
     *
     * @param host the host for this URI
     *
     * @exception MalformedURIException if p_host is not a valid IP address or DNS
     *                                  hostname.
     */
    public void setHost(final String host) throws MalformedURIException {
        if (host == null || host.isEmpty()) {
            if (host != null) {
                regAuthority_ = null;
            }
            host_ = host;
            userinfo_ = null;
            port_ = -1;
            return;
        }
        else if (!isWellFormedAddress(host)) {
            throw new MalformedURIException("Host is not a well formed address!");
        }
        host_ = host;
        regAuthority_ = null;
    }

    /**
     * Set the port for this URI. -1 is used to indicate that the port is not
     * specified, otherwise valid port numbers are between 0 and 65535. If a valid
     * port number is passed in and the host field is null, an exception is thrown.
     *
     * @param port the port number for this URI
     *
     * @exception MalformedURIException if p_port is not -1 and not a valid port
     *                                  number
     */
    public void setPort(final int port) throws MalformedURIException {
        if (port >= 0 && port <= 65535) {
            if (host_ == null) {
                throw new MalformedURIException("Port cannot be set when host is null!");
            }
        }
        else if (port != -1) {
            throw new MalformedURIException("Invalid port number!");
        }
        port_ = port;
    }

    /**
     * Set the path for this URI. If the supplied path is null, then the query
     * string and fragment are set to null as well. If the supplied path includes a
     * query string and/or fragment, these fields will be parsed and set as well.
     * Note that, for URIs following the "generic URI" syntax, the path specified
     * should start with a slash. For URIs that do not follow the generic URI
     * syntax, this method sets the scheme-specific part.
     *
     * @param path the path for this URI (may be null)
     *
     * @exception MalformedURIException if p_path contains invalid characters
     */
    public void setPath(final String path) throws MalformedURIException {
        if (path == null) {
            path_ = null;
            queryString_ = null;
            fragment_ = null;
        }
        else {
            initializePath(path, 0);
        }
    }

    /**
     * Set the query string for this URI. A non-null value is valid only if this is
     * an URI conforming to the generic URI syntax and the path value is not null.
     *
     * @param queryString the query string for this URI
     *
     * @exception MalformedURIException if p_queryString is not null and this URI
     *                                  does not conform to the generic URI syntax
     *                                  or if the path is null
     */
    public void setQueryString(final String queryString) throws MalformedURIException {
        if (queryString == null) {
            queryString_ = null;
        }
        else if (!isGenericURI()) {
            throw new MalformedURIException("Query string can only be set for a generic URI!");
        }
        else if (getPath() == null) {
            throw new MalformedURIException("Query string cannot be set when path is null!");
        }
        else if (!isURIString(queryString)) {
            throw new MalformedURIException("Query string contains invalid character!");
        }
        else {
            queryString_ = queryString;
        }
    }

    /**
     * Set the fragment for this URI. A non-null value is valid only if this is a
     * URI conforming to the generic URI syntax and the path value is not null.
     *
     * @param fragment the fragment for this URI
     *
     * @exception MalformedURIException if p_fragment is not null and this URI does
     *                                  not conform to the generic URI syntax or if
     *                                  the path is null
     */
    public void setFragment(final String fragment) throws MalformedURIException {
        if (fragment == null) {
            fragment_ = null;
        }
        else if (!isGenericURI()) {
            throw new MalformedURIException("Fragment can only be set for a generic URI!");
        }
        else if (getPath() == null) {
            throw new MalformedURIException("Fragment cannot be set when path is null!");
        }
        else if (!isURIString(fragment)) {
            throw new MalformedURIException("Fragment contains invalid character!");
        }
        else {
            fragment_ = fragment;
        }
    }

    /**
     * Determines if the passed-in Object is equivalent to this URI.
     *
     * @param test the Object to test for equality.
     *
     * @return true if p_test is a URI with all values equal to this URI, false
     *         otherwise
     */
    @Override
    public boolean equals(final Object test) {
        if (test instanceof URI) {
            final URI testURI = (URI) test;
            if (((scheme_ == null && testURI.scheme_ == null)
                    || (scheme_ != null && testURI.scheme_ != null && scheme_.equals(testURI.scheme_)))
                    && ((userinfo_ == null && testURI.userinfo_ == null) || (userinfo_ != null
                            && testURI.userinfo_ != null && userinfo_.equals(testURI.userinfo_)))
                    && ((host_ == null && testURI.host_ == null)
                            || (host_ != null && testURI.host_ != null && host_.equals(testURI.host_)))
                    && port_ == testURI.port_
                    && ((path_ == null && testURI.path_ == null)
                            || (path_ != null && testURI.path_ != null && path_.equals(testURI.path_)))
                    && ((queryString_ == null && testURI.queryString_ == null) || (queryString_ != null
                            && testURI.queryString_ != null && queryString_.equals(testURI.queryString_)))
                    && ((fragment_ == null && testURI.fragment_ == null) || (fragment_ != null
                            && testURI.fragment_ != null && fragment_.equals(testURI.fragment_)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the URI as a string specification. See RFC 2396 Section 5.2.
     *
     * @return the URI string specification
     */
    @Override
    public String toString() {
        final StringBuilder uriSpecString = new StringBuilder();

        if (scheme_ != null) {
            uriSpecString.append(scheme_);
            uriSpecString.append(':');
        }
        uriSpecString.append(getSchemeSpecificPart());
        return uriSpecString.toString();
    }

    /**
     * Get the indicator as to whether this URI uses the "generic URI" syntax.
     *
     * @return true if this URI uses the "generic URI" syntax, false otherwise
     */
    public boolean isGenericURI() {
        // presence of the host (whether valid or empty) means
        // double-slashes which means generic uri
        return host_ != null;
    }

    /**
     * Returns whether this URI represents an absolute URI.
     *
     * @return true if this URI represents an absolute URI, false otherwise
     */
    public boolean isAbsoluteURI() {
        // presence of the scheme means absolute uri
        return scheme_ != null;
    }

    /**
     * Determine whether a scheme conforms to the rules for a scheme name. A scheme
     * is conformant if it starts with an alphanumeric, and contains only
     * alphanumerics, '+','-' and '.'.
     *
     * @param scheme the scheme
     *
     * @return true if the scheme is conformant, false otherwise
     */
    private static boolean isConformantSchemeName(final String scheme) {
        if (scheme == null || scheme.trim().isEmpty() || !isAlpha(scheme.charAt(0))) {
            return false;
        }

        char testChar;
        final int schemeLength = scheme.length();
        for (int i = 1; i < schemeLength; ++i) {
            testChar = scheme.charAt(i);
            if (!isSchemeCharacter(testChar)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine whether a string is syntactically capable of representing a valid
     * IPv4 address, IPv6 reference or the domain name of a network host. A valid
     * IPv4 address consists of four decimal digit groups separated by a '.'. Each
     * group must consist of one to three digits. See RFC 2732 Section 3, and RFC
     * 2373 Section 2.2, for the definition of IPv6 references. A hostname consists
     * of domain labels (each of which must begin and end with an alphanumeric but
     * may contain '-') separated &amp; by a '.'. See RFC 2396 Section 3.2.2.
     *
     * @param address the address
     * @return true if the string is a syntactically valid IPv4 address, IPv6
     *         reference or hostname
     */
    public static boolean isWellFormedAddress(final String address) {
        if (address == null) {
            return false;
        }

        final int addrLength = address.length();
        if (addrLength == 0) {
            return false;
        }

        // Check if the host is a valid IPv6reference.
        if (address.startsWith("[")) {
            return isWellFormedIPv6Reference(address);
        }

        // Cannot start with a '.', '-', or end with a '-'.
        if (address.startsWith(".") || address.startsWith("-") || address.endsWith("-")) {
            return false;
        }

        // rightmost domain label starting with digit indicates IP address
        // since top level domain label can only start with an alpha
        // see RFC 2396 Section 3.2.2
        int index = address.lastIndexOf('.');
        if (address.endsWith(".")) {
            index = address.substring(0, index).lastIndexOf('.');
        }

        if (index + 1 < addrLength && isDigit(address.charAt(index + 1))) {
            return isWellFormedIPv4Address(address);
        }
        // hostname = *( domainlabel "." ) toplabel [ "." ]
        // domainlabel = alphanum | alphanum *( alphanum | "-" ) alphanum
        // toplabel = alpha | alpha *( alphanum | "-" ) alphanum

        // RFC 2396 states that hostnames take the form described in
        // RFC 1034 (Section 3) and RFC 1123 (Section 2.1). According
        // to RFC 1034, hostnames are limited to 255 characters.
        if (addrLength > 255) {
            return false;
        }

        // domain labels can contain alphanumerics and '-"
        // but must start and end with an alphanumeric
        char testChar;
        int labelCharCount = 0;

        for (int i = 0; i < addrLength; i++) {
            testChar = address.charAt(i);
            if (testChar == '.') {
                if (!isAlphanum(address.charAt(i - 1)) || (i + 1 < addrLength && !isAlphanum(address.charAt(i + 1)))) {
                    return false;
                }
                labelCharCount = 0;
            }
            else if (!isAlphanum(testChar) && testChar != '-') {
                return false;
            }
            // RFC 1034: Labels must be 63 characters or less.
            else if (++labelCharCount > 63) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Determines whether a string is an IPv4 address as defined by RFC 2373, and
     * under the further constraint that it must be a 32-bit address. Though not
     * expressed in the grammar, in order to satisfy the 32-bit address constraint,
     * each segment of the address cannot be greater than 255 (8 bits of
     * information).
     * </p>
     *
     * <p>
     * <code>IPv4address = 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT</code>
     * </p>
     *
     * @param address the address
     * @return true if the string is a syntactically valid IPv4 address
     */
    public static boolean isWellFormedIPv4Address(final String address) {

        final int addrLength = address.length();
        char testChar;
        int numDots = 0;
        int numDigits = 0;

        // make sure that 1) we see only digits and dot separators, 2) that
        // any dot separator is preceded and followed by a digit and
        // 3) that we find 3 dots
        //
        // RFC 2732 amended RFC 2396 by replacing the definition
        // of IPv4address with the one defined by RFC 2373. - mrglavas
        //
        // IPv4address = 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
        //
        // One to three digits must be in each segment.
        for (int i = 0; i < addrLength; i++) {
            testChar = address.charAt(i);
            if (testChar == '.') {
                if ((i > 0 && !isDigit(address.charAt(i - 1)))
                        || (i + 1 < addrLength && !isDigit(address.charAt(i + 1)))) {
                    return false;
                }
                numDigits = 0;
                if (++numDots > 3) {
                    return false;
                }
            }
            else if (!isDigit(testChar)) {
                return false;
            }
            // Check that that there are no more than three digits
            // in this segment.
            else if (++numDigits > 3) {
                return false;
            }
            // Check that this segment is not greater than 255.
            else if (numDigits == 3) {
                final char first = address.charAt(i - 2);
                final char second = address.charAt(i - 1);
                if (!(first < '2' || (first == '2' && (second < '5' || (second == '5' && testChar <= '5'))))) {
                    return false;
                }
            }
        }
        return numDots == 3;
    }

    /**
     * <p>
     * Determines whether a string is an IPv6 reference as defined by RFC 2732,
     * where IPv6address is defined in RFC 2373. The IPv6 address is parsed
     * according to Section 2.2 of RFC 2373, with the additional constraint that the
     * address be composed of 128 bits of information.
     * </p>
     *
     * <p>
     * <code>IPv6reference = "[" IPv6address "]"</code>
     * </p>
     *
     * <p>
     * Note: The BNF expressed in RFC 2373 Appendix B does not accurately describe
     * section 2.2, and was in fact removed from RFC 3513, the successor of RFC
     * 2373.
     * </p>
     *
     * @param address the address
     * @return true if the string is a syntactically valid IPv6 reference
     */
    public static boolean isWellFormedIPv6Reference(final String address) {

        final int addrLength = address.length();
        final int end = addrLength - 1;

        // Check if string is a potential match for IPv6reference.
        if (!(addrLength > 2 && address.charAt(0) == '[' && address.charAt(end) == ']')) {
            return false;
        }

        // Counter for the number of 16-bit sections read in the address.
        final int[] counter = new int[1];

        // Scan hex sequence before possible '::' or IPv4 address.
        int index = scanHexSequence(address, 1, end, counter);
        if (index == -1) {
            return false;
        }
        // Address must contain 128-bits of information.
        if (index == end) {
            return counter[0] == 8;
        }

        if (index + 1 < end && address.charAt(index) == ':') {
            if (address.charAt(index + 1) == ':') {
                // '::' represents at least one 16-bit group of zeros.
                if (++counter[0] > 8) {
                    return false;
                }
                index += 2;
                // Trailing zeros will fill out the rest of the address.
                if (index == end) {
                    return true;
                }
            }
            // If the second character wasn't ':', in order to be valid,
            // the remainder of the string must match IPv4Address,
            // and we must have read exactly 6 16-bit groups.
            else {
                return (counter[0] == 6) && isWellFormedIPv4Address(address.substring(index + 1, end));
            }
        }
        else {
            return false;
        }

        // 3. Scan hex sequence after '::'.
        final int prevCount = counter[0];
        index = scanHexSequence(address, index, end, counter);

        // We've either reached the end of the string, the address ends in
        // an IPv4 address, or it is invalid. scanHexSequence has already
        // made sure that we have the right number of bits.
        return (index == end) || (index != -1
                && isWellFormedIPv4Address(address.substring((counter[0] > prevCount) ? index + 1 : index, end)));
    }

    /**
     * Helper method for isWellFormedIPv6Reference which scans the hex sequences of
     * an IPv6 address. It returns the index of the next character to scan in the
     * address, or -1 if the string cannot match a valid IPv6 address.
     *
     * @param address the string to be scanned
     * @param index   the beginning index (inclusive)
     * @param end     the ending index (exclusive)
     * @param counter a counter for the number of 16-bit sections read in the
     *                address
     *
     * @return the index of the next character to scan, or -1 if the string cannot
     *         match a valid IPv6 address
     */
    private static int scanHexSequence(final String address, int index, final int end, final int[] counter) {

        char testChar;
        int numDigits = 0;
        final int start = index;

        // Trying to match the following productions:
        // hexseq = hex4 *( ":" hex4)
        // hex4 = 1*4HEXDIG
        for ( ; index < end; ++index) {
            testChar = address.charAt(index);
            if (testChar == ':') {
                // IPv6 addresses are 128-bit, so there can be at most eight sections.
                if (numDigits > 0 && ++counter[0] > 8) {
                    return -1;
                }
                // This could be '::'.
                if (numDigits == 0 || ((index + 1 < end) && address.charAt(index + 1) == ':')) {
                    return index;
                }
                numDigits = 0;
            }
            // This might be invalid or an IPv4address. If it's potentially an IPv4address,
            // backup to just after the last valid character that matches hexseq.
            else if (!isHex(testChar)) {
                if (testChar == '.' && numDigits < 4 && numDigits > 0 && counter[0] <= 6) {
                    final int back = index - numDigits - 1;
                    return (back >= start) ? back : (back + 1);
                }
                return -1;
            }
            // There can be at most 4 hex digits per group.
            else if (++numDigits > 4) {
                return -1;
            }
        }
        return (numDigits > 0 && ++counter[0] <= 8) ? end : -1;
    }

    /**
     * Determine whether a char is a digit.
     *
     * @return true if the char is between '0' and '9', false otherwise
     */
    private static boolean isDigit(final char chr) {
        return chr >= '0' && chr <= '9';
    }

    /**
     * Determine whether a character is a hexadecimal character.
     *
     * @return true if the char is between '0' and '9', 'a' and 'f' or 'A' and 'F',
     *         false otherwise
     */
    private static boolean isHex(final char ch) {
        return ch <= 'f' && (FG_LOOKUP_TABLE[ch] & ASCII_HEX_CHARACTERS) != 0;
    }

    /**
     * Determine whether a char is an alphabetic character: a-z or A-Z
     *
     * @return true if the char is alphabetic, false otherwise
     */
    private static boolean isAlpha(final char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /**
     * Determine whether a char is an alphanumeric: 0-9, a-z or A-Z
     *
     * @return true if the char is alphanumeric, false otherwise
     */
    private static boolean isAlphanum(final char ch) {
        return ch <= 'z' && (FG_LOOKUP_TABLE[ch] & MASK_ALPHA_NUMERIC) != 0;
    }

    /**
     * Determine whether a char is a URI character (reserved or unreserved, not
     * including '%' for escaped octets).
     *
     * @return true if the char is a URI character, false otherwise
     */
    private static boolean isURICharacter(final char ch) {
        return ch <= '~' && (FG_LOOKUP_TABLE[ch] & MASK_URI_CHARACTER) != 0;
    }

    /**
     * Determine whether a char is a scheme character.
     *
     * @return true if the char is a scheme character, false otherwise
     */
    private static boolean isSchemeCharacter(final char ch) {
        return ch <= 'z' && (FG_LOOKUP_TABLE[ch] & MASK_SCHEME_CHARACTER) != 0;
    }

    /**
     * Determine whether a char is a userinfo character.
     *
     * @return true if the char is a userinfo character, false otherwise
     */
    private static boolean isUserinfoCharacter(final char ch) {
        return ch <= 'z' && (FG_LOOKUP_TABLE[ch] & MASK_USERINFO_CHARACTER) != 0;
    }

    /**
     * Determine whether a char is a path character.
     *
     * @return true if the char is a path character, false otherwise
     */
    private static boolean isPathCharacter(final char ch) {
        return ch <= '~' && (FG_LOOKUP_TABLE[ch] & MASK_PATH_CHARACTER) != 0;
    }

    /**
     * Determine whether a given string contains only URI characters (also called
     * "uric" in RFC 2396). uric consist of all reserved characters, unreserved
     * characters and escaped characters.
     *
     * @return true if the string is comprised of uric, false otherwise
     */
    private static boolean isURIString(final String uric) {
        if (uric == null) {
            return false;
        }
        final int end = uric.length();
        for (int i = 0; i < end; i++) {
            final char testChar = uric.charAt(i);
            if (testChar == '%') {
                if (i + 2 >= end || !isHex(uric.charAt(i + 1)) || !isHex(uric.charAt(i + 2))) {
                    return false;
                }
                i += 2;
                continue;
            }
            if (isURICharacter(testChar)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
