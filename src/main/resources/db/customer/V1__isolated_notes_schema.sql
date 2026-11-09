CREATE TYPE content_block_type AS ENUM ('text','image','drawing','table','file_attachment','code_block','checkbox','list_item','audio','video','embed');
CREATE TABLE users (
 user_id UUID PRIMARY KEY,
 username VARCHAR(50) NOT NULL UNIQUE,
 email VARCHAR(100) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE notebooks (
 notebook_id UUID PRIMARY KEY,
 user_id UUID NOT NULL,
 name VARCHAR(255) NOT NULL,
 color VARCHAR(7),
 is_deleted BOOLEAN,
 order_in_user INTEGER NOT NULL,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE section_groups (
 section_group_id UUID PRIMARY KEY,
 notebook_id UUID NOT NULL,
 parent_section_group_id UUID,
 name VARCHAR(255) NOT NULL,
 is_deleted BOOLEAN,
 order_in_parent INTEGER NOT NULL,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE sections (
 section_id UUID PRIMARY KEY,
 notebook_id UUID NOT NULL,
 section_group_id UUID,
 name VARCHAR(255) NOT NULL,
 color VARCHAR(7),
 is_deleted BOOLEAN,
 order_in_parent INTEGER NOT NULL,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE pages (
 page_id UUID PRIMARY KEY,
 section_id UUID NOT NULL,
 parent_page_id UUID,
 title VARCHAR(255) NOT NULL,
 color VARCHAR(7) NOT NULL,
 is_deleted BOOLEAN,
 order_in_section INTEGER NOT NULL,
 last_modified_by_user_id UUID,
 version INTEGER NOT NULL,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE page_content_blocks (
 content_block_id UUID PRIMARY KEY,
 page_id UUID NOT NULL,
 type content_block_type NOT NULL,
 content_data JSONB NOT NULL,
 x_position INTEGER,
 y_position INTEGER,
 width INTEGER,
 height INTEGER,
 z_index INTEGER,
 order_on_page INTEGER NOT NULL,
 last_modified_by_user_id UUID,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE resources (
 resource_id UUID PRIMARY KEY,
 user_id UUID NOT NULL,
 file_name VARCHAR(255) NOT NULL,
 mime_type VARCHAR(100) NOT NULL,
 file_size BIGINT NOT NULL,
 storage_path VARCHAR(500) NOT NULL,
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE tags (
 tag_id UUID PRIMARY KEY,
 user_id UUID,
 name VARCHAR(100) NOT NULL,
 color VARCHAR(7),
 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE customer_identity (id INTEGER PRIMARY KEY CHECK(id=1), instance_id VARCHAR(40) NOT NULL UNIQUE, owner_id UUID NOT NULL);
