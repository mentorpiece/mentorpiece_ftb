#!/usr/bin/env node
const axios = require('axios');
const fs = require('fs').promises;
const path = require('path');
const chokidar = require('chokidar');

class SwaggerSync {
    constructor() {
        this.baseUrl = 'http://localhost:8080';
        this.apiDocsUrl = `${this.baseUrl}/v3/api-docs`;
        this.standaloneFile = path.join(__dirname, 'swagger-ui-standalone.html');
        this.isWatching = process.argv.includes('--watch');
    }

    async fetchApiSpec() {
        try {
            console.log('🔄 Fetching API specification from:', this.apiDocsUrl);
            const response = await axios.get(this.apiDocsUrl, {
                timeout: 10000,
                headers: {
                    'Accept': 'application/json'
                }
            });
            console.log('✅ API specification fetched successfully');
            return response.data;
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                console.error('❌ Connection refused. Is the Spring Boot application running on port 8080?');
            } else if (error.code === 'ENOTFOUND') {
                console.error('❌ Host not found. Check if localhost:8080 is accessible.');
            } else {
                console.error('❌ Error fetching API spec:', error.message);
            }
            throw error;
        }
    }

    async updateStandaloneHtml(apiSpec) {
        try {
            console.log('📝 Reading current standalone HTML file...');
            const currentContent = await fs.readFile(this.standaloneFile, 'utf8');
            
            // Look for the spec property - handle both formats:
            // 1. spec: { (on same line)
            // 2. spec: \n                { (on separate line)
            let specStart = currentContent.indexOf('spec: {');
            let specOffset = 6; // length of 'spec: '
            
            if (specStart === -1) {
                // Try alternative format with newline
                const specPattern = /spec:\s*\n\s*{/;
                const match = currentContent.match(specPattern);
                if (match) {
                    specStart = currentContent.indexOf(match[0]);
                    specOffset = match[0].length - 1; // Don't include the opening brace
                } else {
                    throw new Error('Could not find spec object in HTML file. Looking for "spec: {" or "spec:\\n                {"');
                }
            }
            
            const specEnd = this.findClosingBrace(currentContent, specStart + specOffset);
            
            if (specStart === -1 || specEnd === -1) {
                throw new Error('Could not find spec object boundaries in HTML file');
            }

            // Create the new spec content with proper formatting to match existing indentation
            const beforeSpec = currentContent.substring(0, specStart);
            const afterSpec = currentContent.substring(specEnd + 1);
            
            // Detect indentation from the original file
            const lines = beforeSpec.split('\n');
            const lastLine = lines[lines.length - 1];
            const indentMatch = lastLine.match(/^(\s*)/);
            const baseIndent = indentMatch ? indentMatch[1] : '                ';
            
            // Format the API spec with proper indentation
            const formattedSpec = JSON.stringify(apiSpec, null, 2)
                .split('\n')
                .map((line, index) => {
                    if (index === 0) return line; // First line doesn't need extra indent
                    return baseIndent + line;
                })
                .join('\n');
            
            // Reconstruct the file
            const newContent = beforeSpec + 'spec: \n' + baseIndent + formattedSpec + ',' + afterSpec;

            // Write the updated content
            await fs.writeFile(this.standaloneFile, newContent, 'utf8');
            console.log('✅ Standalone HTML file updated successfully');
            
            return true;
        } catch (error) {
            console.error('❌ Error updating HTML file:', error.message);
            throw error;
        }
    }

    findClosingBrace(content, startPos) {
        let braceCount = 1;
        let pos = startPos;
        
        while (pos < content.length && braceCount > 0) {
            if (content[pos] === '{') {
                braceCount++;
            } else if (content[pos] === '}') {
                braceCount--;
            }
            pos++;
        }
        
        return braceCount === 0 ? pos - 1 : -1;
    }

    async syncOnce() {
        try {
            console.log('\n🚀 Starting Swagger UI sync...');
            const apiSpec = await this.fetchApiSpec();
            await this.updateStandaloneHtml(apiSpec);
            console.log('🎉 Sync completed successfully!\n');
            return true;
        } catch (error) {
            console.error('💥 Sync failed:', error.message);
            return false;
        }
    }

    async startWatcher() {
        console.log('👀 Starting file watcher for automatic sync...');
        console.log('📁 Watching for changes in Spring Boot application...');
        
        // Watch for changes that might affect the API spec
        const watcher = chokidar.watch([
            'src/main/java/**/*.java',
            'target/classes/**/*.class'
        ], {
            ignored: /(^|[\/\\])\../, // ignore dotfiles
            persistent: true,
            ignoreInitial: true
        });

        let syncTimeout;
        const debouncedSync = () => {
            clearTimeout(syncTimeout);
            syncTimeout = setTimeout(() => {
                this.syncOnce();
            }, 2000); // Wait 2 seconds after last change
        };

        watcher.on('change', (filePath) => {
            console.log(`📝 File changed: ${path.relative(__dirname, filePath)}`);
            debouncedSync();
        });

        watcher.on('add', (filePath) => {
            console.log(`➕ File added: ${path.relative(__dirname, filePath)}`);
            debouncedSync();
        });

        // Also sync periodically in case we miss changes
        const periodicSync = setInterval(() => {
            console.log('⏰ Periodic sync check...');
            this.syncOnce();
        }, 30000); // Every 30 seconds

        console.log('✅ Watcher started. Press Ctrl+C to stop.');
        
        // Cleanup on exit
        process.on('SIGINT', () => {
            console.log('\n🛑 Stopping watcher...');
            watcher.close();
            clearInterval(periodicSync);
            process.exit(0);
        });

        return watcher;
    }

    async run() {
        console.log('🔧 Swagger UI Auto-Sync Tool');
        console.log('============================');
        
        if (this.isWatching) {
            // Initial sync
            await this.syncOnce();
            // Start watching
            await this.startWatcher();
        } else {
            // Single sync
            const success = await this.syncOnce();
            process.exit(success ? 0 : 1);
        }
    }
}

// Run the sync tool
const syncer = new SwaggerSync();
syncer.run().catch((error) => {
    console.error('💥 Fatal error:', error.message);
    process.exit(1);
});