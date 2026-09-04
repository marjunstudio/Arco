import Shared
import SwiftUI

@main
struct iOSApp: App {
    // Compose の構成はホストの内側に閉じているので、アプリで1つだけ作って持ち回す。
    private let host: ArcoAppHost

    init() {
        // ArcoAppHost は Koin から依存を引くので、生成より先に立ち上げる。
        initArco()
        host = ArcoAppHost()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(host: host)
        }
    }
}
