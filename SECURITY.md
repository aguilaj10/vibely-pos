# Security Policy

## Supported Versions

We take security seriously. The following versions of Vibely POS are currently being supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

We appreciate responsible disclosure of security vulnerabilities. If you discover a security issue, please follow these steps:

### 1. **Do Not Open a Public Issue**

Security vulnerabilities should **not** be reported via public GitHub issues. This helps prevent exploitation before a fix is available.

### 2. **Report Privately**

Please report security vulnerabilities by emailing: **security@vibely-pos.com** (or open a [private security advisory](https://github.com/your-username/vibely-pos/security/advisories/new) on GitHub)

Include the following information:
- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact
- Any suggested fixes (if applicable)

### 3. **What to Expect**

- **Acknowledgment**: We will acknowledge receipt of your report within 48 hours
- **Assessment**: We will investigate and assess the vulnerability within 5-7 business days
- **Updates**: We will keep you informed of our progress
- **Disclosure**: We will coordinate with you on the disclosure timeline
- **Credit**: You will be credited in the security advisory (unless you prefer to remain anonymous)

### 4. **Disclosure Timeline**

- **0-7 days**: Initial investigation and assessment
- **7-30 days**: Develop and test fix
- **30 days**: Public disclosure (coordinated with reporter)

We aim to resolve critical vulnerabilities within 30 days of initial report.

## Security Best Practices for Users

### 1. **Environment Variables**
- **Never commit** `.env` files or expose credentials in version control
- Use strong, unique secrets for `JWT_SECRET`
- Rotate secrets regularly in production

### 2. **Supabase Configuration**
- Keep `SUPABASE_SERVICE_ROLE_KEY` secure - it bypasses Row Level Security (RLS)
- Use RLS policies for production databases
- Regularly audit database access logs

### 3. **Debug Mode**
- **Never enable `DEBUG_MODE=true` in production**
- Debug mode bypasses authentication and should only be used in local development
- Ensure debug user exists only in development databases

### 4. **Authentication**
- Enforce strong password policies (8+ characters, mixed case, numbers, special characters)
- Implement rate limiting on authentication endpoints
- Use HTTPS in production environments
- Regularly review user access and revoke unused accounts

### 5. **CORS Configuration**
- Restrict CORS to specific domains in production
- Do not use `anyHost()` in production Ktor configuration
- Whitelist only trusted origins

### 6. **Dependencies**
- Keep dependencies up to date
- Run `./gradlew dependencyUpdates` regularly
- Monitor security advisories for dependencies

### 7. **Database**
- Use parameterized queries (we do via Supabase client)
- Enable database backups
- Encrypt sensitive data at rest
- Implement proper RLS policies

### 8. **Network Security**
- Use HTTPS/TLS for all production deployments
- Implement proper firewall rules
- Restrict database access to known IP addresses
- Use VPN for remote database access

## Known Security Considerations

### Debug User
The application includes a debug user with UUID `a2259bb8-d02d-4384-bf2f-bbfca16bade5` for development purposes. **This user should be disabled or removed in production databases.**

### JWT Configuration
JWT tokens expire after 15 minutes (access tokens) and 7 days (refresh tokens). Adjust these values in production based on your security requirements.

### Row Level Security (RLS)
Ensure RLS policies are properly configured in Supabase for multi-tenant deployments. The provided schema includes RLS policies, but they should be reviewed and customized for your specific use case.

## Security Checklist for Production

Before deploying to production, verify:

- [ ] `DEBUG_MODE` is disabled (`false` or unset)
- [ ] Debug user is removed from production database
- [ ] Strong `JWT_SECRET` is configured
- [ ] `SUPABASE_SERVICE_ROLE_KEY` is stored securely (secrets manager)
- [ ] CORS is restricted to production domains
- [ ] HTTPS is enforced
- [ ] Database backups are configured
- [ ] RLS policies are enabled and tested
- [ ] Rate limiting is implemented
- [ ] Monitoring and logging are configured
- [ ] Security headers are configured (CSP, HSTS, etc.)

## Contact

For security-related questions or concerns, please contact:
- **Email**: security@vibely-pos.com
- **GitHub Security Advisories**: [Report a vulnerability](https://github.com/your-username/vibely-pos/security/advisories/new)

---

Thank you for helping keep Vibely POS secure!
