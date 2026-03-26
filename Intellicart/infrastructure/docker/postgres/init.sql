-- Create schemas required by services
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS users;

-- Grant permissions (though samuel_admin is likely owner/superuser)
GRANT ALL PRIVILEGES ON SCHEMA orders TO samuel_admin;
GRANT ALL PRIVILEGES ON SCHEMA users TO samuel_admin;
