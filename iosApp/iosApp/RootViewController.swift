import Shared
import UIKit

/// Compose を全面に敷き、その上にネイティブの `UITabBar` を重ねる。
///
/// タブバーだけをネイティブにしているのは iOS 26 の Liquid Glass を得るため。Compose の root は
/// アプリに1つだけで、タブを切り替えても作り直さない。
final class RootViewController: UIViewController {
    /// タブ id と iOS での見た目の対応。Kotlin が持つのは id と並び順だけで、ラベルと
    /// SF Symbols はこちら側の持ち物。対応が無い id は placeholder のまま出して気付けるようにする。
    private static let appearances: [String: (title: String, symbolName: String)] = [
        "explore": (title: "探索", symbolName: "location.north.fill"),
        "history": (title: "履歴", symbolName: "clock.arrow.circlepath"),
    ]

    private let host: ArcoAppHost
    private let composeViewController: UIViewController
    private let tabIds: [String]
    private let tabBar = UITabBar()
    private var selectionObserver: Task<Void, Never>?

    init(host: ArcoAppHost) {
        self.host = host
        self.composeViewController = host.viewController()
        self.tabIds = host.tabIds()
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("Storyboard からは生成しない")
    }

    deinit {
        selectionObserver?.cancel()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        installComposeViewController()
        installTabBar()
        observeSelectedTab()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        // バーの高さは OS のバージョンで変わるので実測する。Compose へ渡すのはバーが
        // コンテンツを隠している分だけ。safe area の分は Compose が別に持っているので足さない。
        let hiddenHeight = max(0, view.bounds.maxY - tabBar.frame.minY - view.safeAreaInsets.bottom)
        if composeViewController.additionalSafeAreaInsets.bottom != hiddenHeight {
            composeViewController.additionalSafeAreaInsets.bottom = hiddenHeight
        }
    }

    private func installComposeViewController() {
        addChild(composeViewController)
        composeViewController.view.frame = view.bounds
        composeViewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(composeViewController.view)
        composeViewController.didMove(toParent: self)
    }

    private func installTabBar() {
        tabBar.delegate = self
        tabBar.items = tabIds.enumerated().map { index, id in
            let appearance = Self.appearances[id]
            return UITabBarItem(
                title: appearance?.title ?? id,
                image: UIImage(systemName: appearance?.symbolName ?? "questionmark"),
                tag: index
            )
        }
        tabBar.selectedItem = tabBar.items?.first
        tabBar.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(tabBar)
        NSLayoutConstraint.activate([
            tabBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tabBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            tabBar.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
    }

    /// Kotlin 側の現在地をバーへ反映する。プロセスが復帰したときバックスタックが
    /// 探索以外から始まることがあるので、タップの一方向だけでは足りない。
    private func observeSelectedTab() {
        selectionObserver = Task { [weak self, host] in
            do {
                for try await tabId in host.selectedTabId().asAsyncSequence() {
                    self?.applySelectedTab(tabId)
                }
            } catch {
                // StateFlow は完了しないので、ここへ来るのは購読が畳まれたときだけ
            }
        }
    }

    private func applySelectedTab(_ tabId: String) {
        guard
            let index = tabIds.firstIndex(of: tabId),
            let items = tabBar.items,
            items.indices.contains(index)
        else { return }
        tabBar.selectedItem = items[index]
    }
}

extension RootViewController: UITabBarDelegate {
    func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
        guard tabIds.indices.contains(item.tag) else { return }
        // バックスタックが動いた結果は observeSelectedTab() から戻ってくる。ここでは頼むだけ。
        host.selectTab(tabId: tabIds[item.tag])
    }
}
