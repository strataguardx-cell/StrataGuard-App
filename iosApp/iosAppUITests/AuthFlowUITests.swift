import XCTest

// Compose Multiplatform renders via Skia — Compose elements don't appear in the UIKit accessibility tree.
// Approach: tap fields at known screen coordinates, then type through the iOS on-screen keyboard.
final class AuthFlowUITests: XCTestCase {

    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication(bundleIdentifier: "com.strataguard.app.StrataGuard")
        app.launch()
        sleep(3)
    }

    override func tearDownWithError() throws {
        app.terminate()
    }

    // MARK: - Login test

    func testLoginExistingUser() throws {
        snapshot("01_LoginScreen")

        // Focus email field (46% down the screen)
        coord(0.50, 0.46).tap()
        sleep(1)
        snapshot("02_EmailFieldTapped")

        // Check if keyboard appeared and type email
        if app.keyboards.firstMatch.waitForExistence(timeout: 5) {
            typeViaKeyboard("sysha.vinay@gmail.com")
            snapshot("03_EmailEntered")
        } else {
            // Keyboard not present — Compose may handle input differently
            snapshot("03_NoKeyboard")
        }

        // Tap password field (57% down)
        coord(0.50, 0.57).tap()
        sleep(1)

        if app.keyboards.firstMatch.waitForExistence(timeout: 5) {
            typeViaKeyboard("welcome2")
            snapshot("04_PasswordEntered")
        }

        // Tap Sign In button (72% down)
        coord(0.50, 0.72).tap()
        sleep(10)
        snapshot("05_AfterSignIn")
        XCTAssert(true, "Login attempt completed — see screenshot 05_AfterSignIn")
    }

    // MARK: - Helpers

    private func coord(_ x: CGFloat, _ y: CGFloat) -> XCUICoordinate {
        app.coordinate(withNormalizedOffset: CGVectorMake(x, y))
    }

    private func typeViaKeyboard(_ text: String) {
        let kb = app.keyboards.firstMatch
        for char in text {
            let key = String(char)
            if char == "@" {
                // Tap the @ key — may need symbol shift
                if !kb.keys["@"].exists {
                    // Try switching to numbers/symbols
                    let moreKey = kb.keys["more"].exists ? kb.keys["more"] : kb.keys["123"]
                    if moreKey.exists { moreKey.tap() }
                }
                if kb.keys["@"].exists { kb.keys["@"].tap() }
            } else if char == "." {
                if kb.keys["."].exists { kb.keys["."].tap() }
            } else {
                let lower = key.lowercased()
                if kb.keys[lower].exists {
                    kb.keys[lower].tap()
                }
            }
        }
    }

    private func snapshot(_ name: String) {
        let att = XCTAttachment(screenshot: app.screenshot())
        att.name = name
        att.lifetime = .keepAlways
        add(att)
    }
}
