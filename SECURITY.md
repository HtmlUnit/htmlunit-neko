# Security Policy

## Supported Versions

We actively support the following versions of HtmlUnit NekoHTML Parser:

| Version | Supported          |
| ------- | ------------------ |
| 4.x     | :white_check_mark: |
| 3.x     | :white_check_mark: |
| < 3.0   | :x:                |

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security vulnerability in HtmlUnit NekoHTML Parser, please follow these steps:

### 1. Do Not Open a Public Issue

Please **do not** open a public GitHub issue for security vulnerabilities, as this could put users at risk.

### 2. Report Privately

Report security vulnerabilities privately through one of these methods:

- **GitHub Security Advisories**: Use GitHub's [private vulnerability reporting](https://github.com/HtmlUnit/htmlunit-neko/security/advisories/new)
- **Email**: Send an email to the project maintainer at rbri@rbri.de

### 3. What to Include

When reporting a vulnerability, please include:

- A description of the vulnerability
- Steps to reproduce the issue
- Affected versions
- Any potential impact
- Suggested fix (if you have one)
- Your contact information for follow-up questions

### 4. What to Expect

- **Acknowledgment**: We will acknowledge receipt of your report within 3 business days
- **Updates**: We will provide regular updates on the status of the vulnerability
- **Timeline**: We aim to address critical vulnerabilities within 30 days
- **Credit**: With your permission, we will credit you in the security advisory and release notes

## Security Best Practices

When using HtmlUnit NekoHTML Parser:

1. **Keep Updated**: Always use the latest stable version
2. **Monitor Advisories**: Subscribe to security advisories for this repository
3. **Input Validation**: Validate and sanitize HTML input from untrusted sources
4. **Resource Limits**: Implement timeouts and resource limits when parsing untrusted HTML
5. **Dependency Scanning**: Regularly scan your dependencies for known vulnerabilities

## Known Security Issues

### Fixed Vulnerabilities

- **[CVE-2022-29546](https://nvd.nist.gov/vuln/detail/CVE-2022-29546)**: Denial of service via processing instructions (Fixed in 2.61.0+)
- **[CVE-2022-28366](https://nvd.nist.gov/vuln/detail/CVE-2022-28366)**: Denial of service via crafted processing instruction (Fixed in 2.27+)

## Security Update Process

When a security vulnerability is confirmed:

1. We develop and test a fix
2. We prepare a security advisory
3. We release a patched version
4. We publish the security advisory
5. We notify users through multiple channels

## Questions?

If you have questions about this security policy or the security of HtmlUnit NekoHTML Parser, please open a discussion or contact the maintainers.

## Attribution

Thank you to all security researchers who have responsibly disclosed vulnerabilities to us.
