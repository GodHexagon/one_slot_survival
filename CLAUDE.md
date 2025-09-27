# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Minecraft Forge mod project called "One Slot Survival" (currently using the example mod template). It's built using Java 21 and targets Minecraft 1.21.8 with Forge 58.1.0.

The mod currently includes example blocks, items, and creative tabs that should be replaced with actual "One Slot Survival" content.

## Development Commands

### Build and Development
- `./gradlew build` - Build the mod (compile, test, and create JAR)
- `./gradlew clean` - Clean build artifacts
- `./gradlew jar` - Create mod JAR file
- `./gradlew classes` - Compile main classes only

### Running the Mod
- `./gradlew runClient` - Launch Minecraft client with the mod
- `./gradlew runServer` - Launch dedicated server with the mod
- `./gradlew runData` - Run data generators
- `./gradlew runGameTestServer` - Run game tests

### IDE Setup
- `./gradlew genIntellijRuns` - Generate IntelliJ run configurations
- `./gradlew genEclipseRuns` - Generate Eclipse run configurations
- `./gradlew idea` - Generate IntelliJ project files
- `./gradlew eclipse` - Generate Eclipse project files

### Testing and Verification
- `./gradlew test` - Run unit tests
- `./gradlew check` - Run all verification tasks

## 計画

1. プレイヤーのホットバーは１スロットだけ（これをメインハンドと定義する）になり、それ以外のインベントリとホットバー計３５スロットを使用禁止する。
   * メインハンドがいっぱいになった場合、バニラでインベントリがいっぱいになっときみたいに、新たにアイテムを拾うことができないくなる。
   * メインハンド以外のスロットを保持（ホットバー選択）できない。
1. プレイヤーごとに機能の有効性を変更できるようにする。
   * 管理者権限を持つ者だけが実行できる、管理者アクションの一つとする。
1. プレイヤーはロールスロットを利用できるようにする。
   * プレイヤーはメインハンドのほかに、ロールスロットがホットバーとしてあり、ホットバー選択できる。
     * メインスロットの右側に３つある。
     * つるはししか入れられない。
     * ゲーム中は通常のホットバーと同じ挙動をする。
     * インベントリ中では、防具スロットのように専用スロットとしての挙動をする。
     * ドロップしたつるはしを拾う際は普通に入る。
     * オフハンドからつるはし以外をFキーで直接入れようしても、なにも起こらない。
1. 右から１つ目と２つ目のロールスロットはシャベルしか入らないようにする。
1. プレイヤーに割り当てられたメインロールによって、ロールスロットに入れることができるアイテムが変わるようにする。
    * 今までのロールスロットはMinerのものとし、新たにWarriorを追加する。
1. 管理者アクションに、各プレイヤーのロール変更を加える。
1. メインロールに紐づくEXPとレベルアップがあり、これによりロールスロットをカスタムできるようにする。
1. メインロールの種類を増やす。
1. サブロールを追加する。
