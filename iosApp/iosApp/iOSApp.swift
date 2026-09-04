import Shared
import SwiftUI

@main
struct iOSApp: App {
    // Compose の構成はホストの内側に閉じているので、アプリで1つだけ作って持ち回す。
    private let host = ArcoAppHost()

    var body: some Scene {
        WindowGroup {
            ContentView(host: host)
        }
    }
}
