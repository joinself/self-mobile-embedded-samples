//
//  ContentView.swift
//  SwiftUIExample
//
//  Created by Long Pham on 24/6/24.
//

import SwiftUI
import self_ios_sdk

struct ContentView: View {
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Hello, world!")
            Button {
            } label: {
                Text("Call Self iOS SDK.")
            }

        }
        .padding()
    }
}

#Preview {
    ContentView()
}
