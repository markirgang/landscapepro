#!/usr/bin/env python3
"""
LandscapePro AI - Local Server Launcher
=======================================
This script starts a lightweight local web server and automatically opens the
LandscapePro AI application in your default web browser.

Why is this needed?
-------------------
Modern web browsers enforce strict security policies (CORS) on the file:// protocol.
If you open index.html directly by double-clicking it, drawing template images onto 
the canvas and trying to export them taints the canvas, causing "AI Generation" 
or export actions to fail due to browser security restrictions.

Running this script serves the files over http://localhost, which satisfies the
browser's security checks and allows all canvas operations to run successfully.
"""

import http.server
import socketserver
import webbrowser
import threading
import time
import sys

def find_and_start_server():
    port = 8000
    max_port = 8100
    
    # We will try ports starting from 8000 up to 8100
    while port <= max_port:
        try:
            # We want to serve files from the directory where this script is located.
            # SimpleHTTPRequestHandler serves files from the current working directory.
            handler = http.server.SimpleHTTPRequestHandler
            
            # Allow reusing address immediately after server is shut down
            socketserver.TCPServer.allow_reuse_address = True
            
            httpd = socketserver.TCPServer(("", port), handler)
            return httpd, port
        except OSError:
            # OSError is raised if the port is already in use
            port += 1
            
    print("Error: Could not find an available port between 8000 and 8100.", file=sys.stderr)
    sys.exit(1)

def main():
    print("=" * 60)
    print("             LandscapePro AI Launcher")
    print("=" * 60)
    print("Starting local development server...")
    
    httpd, port = find_and_start_server()
    url = f"http://localhost:{port}/index.html"
    
    # Start the server in a daemon thread so it shuts down automatically when the main script exits
    server_thread = threading.Thread(target=httpd.serve_forever, daemon=True)
    server_thread.start()
    
    print(f"\n[SUCCESS] Local server is running at: {url}")
    print("Opening application in your default web browser...")
    
    # Give the server thread a tiny fraction of a second to spin up
    time.sleep(0.3)
    
    # Open the browser
    webbrowser.open(url)
    
    print("\nKeep this terminal window open while using the app.")
    print("Press Ctrl+C inside this terminal to stop the server at any time.")
    print("=" * 60)
    
    try:
        # Keep main thread alive
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n\nShutting down server. Thank you for using LandscapePro AI!")
        # socketserver handles cleanup
        httpd.shutdown()
        sys.exit(0)

if __name__ == "__main__":
    main()
