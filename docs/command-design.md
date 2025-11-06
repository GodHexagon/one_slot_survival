# コマンド設計

```
- oneslot
    - [status]: 自身の状態
    - role [list]: ロール一覧
    - role detail [<Role>]: 特定のロールの情報
    - slot detail <Slot> [role <Role>]: 特定のスロットの情報
    - changerole to <Role> from: Roleを変更するとスロットがリセットされる旨の警告
    - changerole to <Role> from <Current Role>
    
    - admin
        - validity
        - role
        - level
            - mainRole
            - subRole
        - xp
        - distribute

    - world
        - defaultValidity
        - keepRoleGrowthment
        - RoleChanging
            - main
            - sub
        - firstRole
        - bonusItem
        - restoreDefaultSettings
```
