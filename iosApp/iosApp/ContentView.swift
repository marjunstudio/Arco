import Shared
import SwiftUI

private struct RootView: UIViewControllerRepresentable {
    let host: ArcoAppHost

    func makeUIViewController(context: Self.Context) -> RootViewController {
        RootViewController(host: host)
    }

    func updateUIViewController(_ uiViewController: RootViewController, context: Self.Context) {}
}

struct ContentView: View {
    let host: ArcoAppHost

    var body: some View {
        // safe area の面倒は RootViewController が見るので、SwiftUI 側では避けない。
        RootView(host: host)
            .ignoresSafeArea()
    }
}
